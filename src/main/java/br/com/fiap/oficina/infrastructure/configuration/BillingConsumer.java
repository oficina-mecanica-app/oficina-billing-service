package br.com.fiap.oficina.infrastructure.configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.com.fiap.oficina.application.service.PaymentService;
import br.com.fiap.oficina.infrastructure.persistence.entity.Payment;
import br.com.fiap.oficina.infrastructure.persistence.repository.PaymentRepository;

/**
 * Recebe as OS criadas e gera a cobrança no Mercado Pago.
 *
 * - Idempotente: se já existe pagamento para a OS (reentrega da mensagem),
 *   não cria uma segunda cobrança.
 * - Nenhuma exceção é engolida: falhas são propagadas para o retry do listener
 *   e, esgotadas as tentativas, a mensagem vai para a DLQ (ver RabbitMQConfig).
 */
@Component
public class BillingConsumer {

    private static final Logger log = LoggerFactory.getLogger(BillingConsumer.class);

    private final PaymentRepository repository;
    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    public BillingConsumer(PaymentRepository repository, PaymentService paymentService,
            RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.paymentService = paymentService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "os-created-queue")
    public void receiveMessage(Map<String, Object> message) {
        Long osId = parseLong(message.get("id"), "id");
        BigDecimal amount = parseAmount(message.get("amount"));

        if (repository.findByServiceOrderId(osId).isPresent()) {
            log.info("Cobrança da OS {} já existe; mensagem duplicada ignorada", osId);
            return;
        }

        log.info("Gerando cobrança da OS {} no valor de {}", osId, amount);
        String paymentUrl = paymentService.createPaymentPreference(osId, amount);

        Payment payment = new Payment();
        payment.setServiceOrderId(osId);
        payment.setStatus("PENDING");
        payment.setAmount(amount);
        payment.setMercadoPagoId(paymentUrl);
        payment.setCreatedAt(LocalDateTime.now());
        repository.save(payment);

        Map<String, Object> linkEvent = new HashMap<>();
        linkEvent.put("serviceOrderId", osId);
        linkEvent.put("paymentUrl", paymentUrl);
        rabbitTemplate.convertAndSend("payment-link-generated-queue", linkEvent);

        log.info("Link de pagamento da OS {} publicado", osId);
    }

    private Long parseLong(Object value, String field) {
        if (value == null) {
            throw new AmqpRejectAndDontRequeueException("Mensagem sem o campo " + field);
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new AmqpRejectAndDontRequeueException("Campo " + field + " inválido: " + value, e);
        }
    }

    private BigDecimal parseAmount(Object value) {
        if (value == null) {
            throw new AmqpRejectAndDontRequeueException("Mensagem sem o campo amount");
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new AmqpRejectAndDontRequeueException("Campo amount inválido: " + value, e);
        }
    }
}
