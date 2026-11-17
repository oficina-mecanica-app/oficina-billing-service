package br.com.fiap.oficina.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.receiveMessage(message));

        verify(paymentService, never()).createPaymentPreference(any(), any());
    }

    @Test
    public void naoDeveGerarCobrancaDuplicadaQuandoMensagemForReentregue() {
        Map<String, Object> message = new HashMap<>();
        message.put("id", 1L);
        message.put("amount", "150.00");

        when(repository.findByServiceOrderId(1L)).thenReturn(Optional.of(new Payment()));

        consumer.receiveMessage(message);

        verify(paymentService, never()).createPaymentPreference(any(), any());
        verify(repository, never()).save(any(Payment.class));
        verify(rabbitTemplate, never()).convertAndSend(eq("payment-link-generated-queue"), any(Map.class));
    }

    @Test
    public void devePropagarFalhaDoMercadoPagoParaPermitirRetry() {
        Map<String, Object> message = new HashMap<>();
        message.put("id", 2L);
        message.put("amount", "99.90");

        when(paymentService.createPaymentPreference(eq(2L), any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("API indisponível"));

        assertThrows(IllegalStateException.class, () -> consumer.receiveMessage(message));

        verify(repository, never()).save(any(Payment.class));
    }
}