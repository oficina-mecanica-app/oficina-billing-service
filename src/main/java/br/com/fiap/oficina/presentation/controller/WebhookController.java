package br.com.fiap.oficina.presentation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

@RestController
@RequestMapping("/billing")
public class WebhookController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestParam(value = "data.id", required = false) Long dataId,
            @RequestParam(value = "type", required = false) String type) {

        if ("payment".equals(type) && dataId != null) {
            try {
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(dataId);

                Long osId = Long.valueOf(payment.getExternalReference());
                Map<String, Object> event = new HashMap<>();
                event.put("serviceOrderId", osId);

                if ("approved".equals(payment.getStatus())) {
                    System.out.println("✅ WEBHOOK: Pagamento REAL aprovado para OS " + osId);
                    event.put("status", "PAID");
                    rabbitTemplate.convertAndSend("os-payment-approved-queue", event);
                    rabbitTemplate.convertAndSend("execution-payment-approved-queue", event);
                } 
                else if ("rejected".equals(payment.getStatus()) || "cancelled".equals(payment.getStatus())) {
                    System.out.println("❌ WEBHOOK: Pagamento RECUSADO para OS " + osId);
                    event.put("status", "REFUSED");
                    rabbitTemplate.convertAndSend("payment-refused-queue", event);
                }

            } catch (Exception e) {
                System.err.println("Erro ao processar webhook do MP: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok("OK");
    }
}