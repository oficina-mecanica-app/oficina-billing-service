package br.com.fiap.oficina.infrastructure.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /** Fila que recebe as mensagens que falharam em todas as tentativas. */
    public static final String DEAD_LETTER_QUEUE = "oficina-billing-service.dlq";
    
    @Bean
    public Queue osCreatedQueue() {
        return new Queue("os-created-queue", true);
    }

    @Bean
    public Queue paymentApprovedQueue() {
        return new Queue("os-payment-approved-queue", true);
    }

    @Bean
    public Queue executionPaymentApprovedQueue() {
        return new Queue("execution-payment-approved-queue", true);
    }

    @Bean
    public Queue paymentRefusedQueue() {
        return new Queue("payment-refused-queue", true);
    }

    @Bean
    public Queue executionFinishedQueue() {
        return new Queue("execution-finished-queue", true);
    }

    @Bean
    public Queue paymentLinkGeneratedQueue() {
        return new Queue("payment-link-generated-queue", true);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    /**
     * Esgotadas as tentativas do listener, republica a mensagem na DLQ do serviço.
     * A mensagem chega com headers de diagnóstico (x-exception-message,
     * x-exception-stacktrace, x-original-routingKey), o que permite investigar
     * e reprocessar depois. O Spring Boot usa este bean automaticamente no retry.
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "", DEAD_LETTER_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}