package com.seaman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.model.external.response.OmiseChargeResponse;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class OmisePaymentClient {
    private static final String CURRENCY = "THB";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${omise.api-url:https://api.omise.co}")
    private String apiUrl;

    @Value("${omise.secret-key:}")
    private String secretKey;

    public OmiseChargeResponse createCharge(BigDecimal amount, String requestNo,
                                            DocumentRenewalPaymentRequest request) {
        requireSecretKey();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("amount", toSubunit(amount));
        form.add("currency", CURRENCY);
        form.add("description", "Document renewal " + requestNo);
        form.add("metadata[request_no]", requestNo);
        form.add("metadata[idempotency_key]", request.getIdempotencyKey());
        form.add("source[type]", request.getPaymentMethod());
        if (request.getPlatformType() != null && !request.getPlatformType().trim().isEmpty()) {
            form.add("source[platform_type]", request.getPlatformType().trim());
        }
        if (request.getReturnUri() != null && !request.getReturnUri().trim().isEmpty()) {
            form.add("return_uri", request.getReturnUri().trim());
        }
        HttpHeaders headers = headers();
        headers.set("Idempotency-Key", request.getIdempotencyKey());
        JsonNode response = restTemplate.postForObject(
                apiUrl + "/charges", new HttpEntity<>(form, headers), JsonNode.class);
        return mapCharge(response);
    }

    public OmiseChargeResponse retrieveCharge(String chargeId) {
        requireSecretKey();
        JsonNode response = restTemplate.getForObject(
                apiUrl + "/charges/" + chargeId, JsonNode.class);
        return mapCharge(response);
    }

    public OmiseChargeResponse mapCharge(JsonNode charge) {
        if (charge == null || charge.get("id") == null) {
            throw new BusinessException(AppStatus.EXCEPTION_TECHNICAL, "omiseCharge");
        }
        OmiseChargeResponse response = new OmiseChargeResponse();
        response.setId(text(charge, "id"));
        response.setStatus(text(charge, "status"));
        response.setAuthorizeUri(text(charge, "authorize_uri"));
        response.setReturnUri(text(charge, "return_uri"));
        response.setFailureCode(text(charge, "failure_code"));
        response.setFailureMessage(text(charge, "failure_message"));
        response.setLivemode(bool(charge, "livemode"));
        response.setExpiresAt(date(charge, "expires_at"));
        response.setPaidAt(date(charge, "paid_at"));
        JsonNode transaction = charge.get("transaction");
        if (transaction != null && transaction.hasNonNull("id")) {
            response.setTransactionId(transaction.get("id").asText());
        }
        JsonNode source = charge.get("source");
        if (source != null) {
            response.setSourceId(text(source, "id"));
            JsonNode image = source.path("scannable_code").path("image");
            if (!image.isMissingNode() && image.hasNonNull("download_uri")) {
                response.setQrCodeDownloadUri(image.get("download_uri").asText());
            }
        }
        response.setRaw(charge);
        return response;
    }

    public String rawJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.EXCEPTION_TECHNICAL, "omiseResponse");
        }
    }

    public String qrCodeDownloadUri(String rawChargeJson) {
        if (rawChargeJson == null || rawChargeJson.trim().isEmpty()) {
            return null;
        }
        try {
            return mapCharge(objectMapper.readTree(rawChargeJson)).getQrCodeDownloadUri();
        } catch (Exception ex) {
            return null;
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String token = Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + token);
        return headers;
    }

    private void requireSecretKey() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_TECHNICAL, "omise.secret-key");
        }
    }

    private String toSubunit(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0).toPlainString();
    }

    private String text(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private Boolean bool(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asBoolean() : null;
    }

    private Date date(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        return Date.from(Instant.parse(node.get(field).asText()));
    }
}
