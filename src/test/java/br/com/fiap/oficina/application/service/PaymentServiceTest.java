package br.com.fiap.oficina.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(paymentService, "accessToken", "test-token");
    }

    @Test
    public void deveCriarPreferenciaDePagamento() {
        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    Preference preference = new Preference();
                    ReflectionTestUtils.setField(preference, "initPoint", "http://mp.com/init");
                    when(mock.create(any(PreferenceRequest.class))).thenReturn(preference);
                })) {

            String result = paymentService.createPaymentPreference(1L, new BigDecimal("100.00"));

            assertEquals("http://mp.com/init", result);
        }
    }

    @Test
    public void deveTratarExcecaoAoCriarPreferencia() {
        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> {
                    when(mock.create(any(PreferenceRequest.class))).thenThrow(new RuntimeException());
                })) {

            String result = paymentService.createPaymentPreference(1L, new BigDecimal("100.00"));

            assertNull(result);
        }
    }
}