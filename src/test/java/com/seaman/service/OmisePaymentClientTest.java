package com.seaman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    void retrieveChargeAuthenticatesWithOmiseSecretKey() throws Exception {
        String body = "{\"id\":\"chrg_test_123\",\"status\":\"pending\"}";
        when(restTemplate.exchange(eq("https://api.omise.co/charges/chrg_test_123"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(new ObjectMapper().readTree(body)));

        client.retrieveCharge("chrg_test_123");

        ArgumentCaptor<HttpEntity> entityCaptor = forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("https://api.omise.co/charges/chrg_test_123"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(JsonNode.class));
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        String expectedToken = Base64.getEncoder().encodeToString(
                "skey_test_123:".getBytes(StandardCharsets.UTF_8));
        assertEquals("Basic " + expectedToken, headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void createChargeRejectsPublicKeyBeforeCallingOmise() {
        ReflectionTestUtils.setField(client, "secretKey", "pkey_test_123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> client.createCharge(new BigDecimal("1500.00"),
                        "260700001", promptPayRequest()));

        assertEquals(AppStatus.EXCEPTION_TECHNICAL, exception.getCode());
        assertTrue(exception.getMessage().contains("omise.secret-key.type"));
        verifyNoInteractions(restTemplate);
    }

    private DocumentRenewalPaymentRequest promptPayRequest() {
        DocumentRenewalPaymentRequest request = new DocumentRenewalPaymentRequest();
        request.setPaymentMethod("promptpay");
        request.setIdempotencyKey("renewal-260700001-attempt-1");
        return request;
    }
}
