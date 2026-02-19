package br.com.fiap.oficina.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

public class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    public void deveCriarOsCreatedQueue() {
        Queue queue = config.osCreatedQueue();

        assertNotNull(queue);
        assertEquals("os-created-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarPaymentApprovedQueue() {
        Queue queue = config.paymentApprovedQueue();

        assertNotNull(queue);
        assertEquals("os-payment-approved-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarExecutionPaymentApprovedQueue() {
        Queue queue = config.executionPaymentApprovedQueue();

        assertNotNull(queue);
        assertEquals("execution-payment-approved-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarPaymentRefusedQueue() {
        Queue queue = config.paymentRefusedQueue();

        assertNotNull(queue);
        assertEquals("payment-refused-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarExecutionFinishedQueue() {
        Queue queue = config.executionFinishedQueue();

        assertNotNull(queue);
        assertEquals("execution-finished-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarPaymentLinkGeneratedQueue() {
        Queue queue = config.paymentLinkGeneratedQueue();

        assertNotNull(queue);
        assertEquals("payment-link-generated-queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    public void deveCriarJsonMessageConverter() {
        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }
}