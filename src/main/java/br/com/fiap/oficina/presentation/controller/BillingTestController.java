package br.com.fiap.oficina.presentation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing/test")
public class BillingTestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/refuse/{osId}")
    public ResponseEntity<String> simulateRefusal(@PathVariable Long osId) {
        
        System.out.println("❌ Simulando recusa de pagamento para OS: " + osId);
        
        // Dispara evento de compensação
        Map<String, Object> event = new HashMap<>();
        event.put("osId", osId);
        event.put("reason", "Cartão Recusado / Saldo Insuficiente");
        
        rabbitTemplate.convertAndSend("payment-refused-queue", event);

        return ResponseEntity.ok("Evento de recusa enviado.");
    }
}