package br.com.fiap.oficina.presentation.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.oficina.infrastructure.persistence.entity.Payment;
import br.com.fiap.oficina.infrastructure.persistence.repository.PaymentRepository;

@RestController
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private PaymentRepository repository;

    @GetMapping
    public List<Payment> listAll() {
        return repository.findAll();
    }

    @PostMapping("/pay/{osId}")
    public ResponseEntity<Payment> processPayment(@PathVariable Long osId, @RequestParam Double value) {
        Payment payment = new Payment();
        payment.setServiceOrderId(osId);
        payment.setAmount(BigDecimal.valueOf(value));
        payment.setStatus("APPROVED");
        
        return ResponseEntity.ok(repository.save(payment));
    }
}