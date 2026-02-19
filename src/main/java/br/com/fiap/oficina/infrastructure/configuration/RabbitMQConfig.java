package br.com.fiap.oficina.infrastructure.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}