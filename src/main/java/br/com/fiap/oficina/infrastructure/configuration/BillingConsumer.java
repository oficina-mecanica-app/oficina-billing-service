package br.com.fiap.oficina.infrastructure.configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.fiap.oficina.application.service.PaymentService;
import br.com.fiap.oficina.infrastructure.persistence.entity.Payment;
import br.com.fiap.oficina.infrastructure.persistence.repository.PaymentRepository;

@Component
public class BillingConsumer {

    @Autowired
    private PaymentRepository repository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "os-created-queue")
    public void receiveMessage(Map<String, Object> message) {
        try {
            Long osId = Long.valueOf(message.get("id").toString());
            
            BigDecimal amount = new BigDecimal(message.get("amount").toString());

            System.out.println("💰 Billing recebeu OS: " + osId + " Valor: " + amount);

            String paymentUrl = paymentService.createPaymentPreference(osId, amount);

            Payment payment = new Payment();
            payment.setServiceOrderId(osId);
            payment.setStatus("PENDING");
            payment.setAmount(amount);
            payment.setMercadoPagoId(paymentUrl);
            payment.setCreatedAt(LocalDateTime.now());
            repository.save(payment);

            System.out.println("Link de pagamento gerado: " + paymentUrl);

            Map<String, Object> linkEvent = new HashMap<>();
            linkEvent.put("serviceOrderId", osId);
            linkEvent.put("paymentUrl", paymentUrl);

            rabbitTemplate.convertAndSend("payment-link-generated-queue", linkEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void simulatePaymentConfirmation(Long osId) {
        Map<String, Object> event = new HashMap<>();
        event.put("serviceOrderId", osId);
        event.put("status", "PAID");

        rabbitTemplate.convertAndSend("os-payment-approved-queue", event);
        rabbitTemplate.convertAndSend("execution-payment-approved-queue", event);
        System.out.println("Simulacao: Pagamento confirmado para OS " + osId);
    }
}