package br.com.fiap.oficina.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

@ExtendWith(MockitoExtension.class)
public class WebhookControllerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WebhookController controller;

    @Test
    public void deveProcessarPagamentoAprovado() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    Payment mockPayment = new Payment();
                    ReflectionTestUtils.setField(mockPayment, "externalReference", "10");
                    ReflectionTestUtils.setField(mockPayment, "status", "approved");
                    when(mock.get(123L)).thenReturn(mockPayment);
                })) {

            ResponseEntity<String> response = controller.receiveWebhook(123L, "payment");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(rabbitTemplate, times(1)).convertAndSend(eq("os-payment-approved-queue"), any(Map.class));
            verify(rabbitTemplate, times(1)).convertAndSend(eq("execution-payment-approved-queue"), any(Map.class));
        }
    }

    @Test
    public void deveProcessarPagamentoRecusado() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    Payment mockPayment = new Payment();
                    ReflectionTestUtils.setField(mockPayment, "externalReference", "20");
                    ReflectionTestUtils.setField(mockPayment, "status", "rejected");
                    when(mock.get(456L)).thenReturn(mockPayment);
                })) {

            ResponseEntity<String> response = controller.receiveWebhook(456L, "payment");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(rabbitTemplate, times(1)).convertAndSend(eq("payment-refused-queue"), any(Map.class));
        }
    }

    @Test
    public void deveProcessarPagamentoCancelado() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    Payment mockPayment = new Payment();
                    ReflectionTestUtils.setField(mockPayment, "externalReference", "30");
                    ReflectionTestUtils.setField(mockPayment, "status", "cancelled");
                    when(mock.get(789L)).thenReturn(mockPayment);
                })) {

            ResponseEntity<String> response = controller.receiveWebhook(789L, "payment");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(rabbitTemplate, times(1)).convertAndSend(eq("payment-refused-queue"), any(Map.class));
        }
    }

    @Test
    public void deveIgnorarWebhookQuandoNaoForPayment() {
        ResponseEntity<String> response = controller.receiveWebhook(123L, "plan");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(Map.class));
    }

    @Test
    public void deveTratarExcecaoNoWebhook() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(999L)).thenThrow(new RuntimeException());
                })) {

            ResponseEntity<String> response = controller.receiveWebhook(999L, "payment");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(Map.class));
        }
    }
}