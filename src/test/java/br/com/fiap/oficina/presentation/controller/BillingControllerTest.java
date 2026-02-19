package br.com.fiap.oficina.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.fiap.oficina.infrastructure.persistence.entity.Payment;
import br.com.fiap.oficina.infrastructure.persistence.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class BillingControllerTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private BillingController controller;

    @Test
    public void deveListarTodosOsPagamentos() {
        when(repository.findAll()).thenReturn(List.of(new Payment()));

        List<Payment> result = controller.listAll();

        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void deveProcessarPagamentoComSucesso() {
        Payment mockPayment = new Payment();
        mockPayment.setServiceOrderId(10L);
        mockPayment.setAmount(BigDecimal.valueOf(150.0));
        mockPayment.setStatus("APPROVED");

        when(repository.save(any(Payment.class))).thenReturn(mockPayment);

        ResponseEntity<Payment> response = controller.processPayment(10L, 150.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getServiceOrderId());
        assertEquals(BigDecimal.valueOf(150.0), response.getBody().getAmount());
        assertEquals("APPROVED", response.getBody().getStatus());
        verify(repository, times(1)).save(any(Payment.class));
    }
}