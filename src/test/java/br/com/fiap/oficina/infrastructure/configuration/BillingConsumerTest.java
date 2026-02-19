package br.com.fiap.oficina.infrastructure.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import br.com.fiap.oficina.application.service.PaymentService;
import br.com.fiap.oficina.infrastructure.persistence.entity.Payment;
import br.com.fiap.oficina.infrastructure.persistence.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class BillingConsumerTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BillingConsumer consumer;

    @Test
    public void deveProcessarMensagemEGerarLink() {
        Map<String, Object> message = new HashMap<>();
        message.put("id", 1L);
        message.put("amount", "150.00");

        when(paymentService.createPaymentPreference(eq(1L), any(BigDecimal.class))).thenReturn("http://link");
        when(repository.save(any(Payment.class))).thenReturn(new Payment());

        consumer.receiveMessage(message);

        verify(paymentService, times(1)).createPaymentPreference(eq(1L), any(BigDecimal.class));
        verify(repository, times(1)).save(any(Payment.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("payment-link-generated-queue"), any(Map.class));
    }

    @Test
    public void deveTratarExcecaoAoProcessarMensagem() {
        Map<String, Object> message = new HashMap<>();
        message.put("id", 1L);

        consumer.receiveMessage(message);

        verify(paymentService, never()).createPaymentPreference(any(), any());
    }

    @Test
    public void deveSimularConfirmacaoDePagamento() {
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(consumer, "simulatePaymentConfirmation", 1L);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("os-payment-approved-queue"), any(java.util.Map.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("execution-payment-approved-queue"), any(java.util.Map.class));
    }
}