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

    @Value("${mercadopago.access.token}")
    private String accessToken;

    public String createPaymentPreference(Long osId, BigDecimal amount) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            String ngrokUrl = "https://5d56-187-7-158-146.ngrok-free.app";

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(osId.toString())
                    .title("Ordem de Servico " + osId)
                    .quantity(1)
                    .unitPrice(amount)
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(osId.toString())
                    .notificationUrl(ngrokUrl + "/billing/webhook")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}