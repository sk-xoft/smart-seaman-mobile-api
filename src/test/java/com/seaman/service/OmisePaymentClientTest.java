package com.seaman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OmisePaymentClientTest {
    private RestTemplate restTemplate;
    private OmisePaymentClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new OmisePaymentClient(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(client, "apiUrl", "https://api.omise.co");
        ReflectionTestUtils.setField(client, "secretKey", "skey_test_123");
    }

    @Test
    void createChargeWrapsOmiseHttpErrorWithoutLeakingRawException() {
        String body = "{\"object\":\"error\",\"code\":\"authentication_failure\","
                + "\"message\":\"authentication failed\"}";
        HttpClientErrorException omiseError = new HttpClientErrorException(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
        when(restTemplate.postForObject(eq("https://api.omise.co/charges"),
                any(HttpEntity.class), eq(JsonNode.class))).thenThrow(omiseError);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> client.createCharge(new BigDecimal("1500.00"),
                        "260700001", promptPayRequest()));

        assertEquals(AppStatus.EXCEPTION_TECHNICAL, exception.getCode());
        assertTrue(exception.getMessage().contains("omise.createCharge:401"));
        assertTrue(exception.getMessage().contains("authentication_failure"));
        assertTrue(exception.getMessage().contains("authentication failed"));
    }

    private DocumentRenewalPaymentRequest promptPayRequest() {
        DocumentRenewalPaymentRequest request = new DocumentRenewalPaymentRequest();
        request.setPaymentMethod("promptpay");
        request.setIdempotencyKey("renewal-260700001-attempt-1");
        return request;
    }
}
