package br.com.fiap.oficina.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

@Service
public class PaymentService {

    /** Vem da variável de ambiente MERCADOPAGO_ACCESS_TOKEN. Nunca versione o token. */
    @Value("${mercadopago.access.token}")
    private String accessToken;

    /** URL pública do serviço; o Mercado Pago chama {notificationUrl}/billing/webhook. */
    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    /**
     * Cria a preferência de pagamento e devolve o link de checkout.
     *
     * @throws IllegalStateException se o token não estiver configurado ou a API
     *         do Mercado Pago falhar (o consumidor da fila tenta de novo).
     */
    public String createPaymentPreference(Long osId, BigDecimal amount) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("MERCADOPAGO_ACCESS_TOKEN não configurado");
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(osId.toString())
                    .title("Ordem de Servico " + osId)
                    .quantity(1)
                    .unitPrice(amount)
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            String webhookUrl = (notificationUrl == null || notificationUrl.isBlank())
                    ? null
                    : notificationUrl + "/billing/webhook";

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(osId.toString())
                    .notificationUrl(webhookUrl)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criar preferência no Mercado Pago para a OS " + osId, e);
        }
    }
}
