package com.seaman.repository;

import com.seaman.entity.PaymentTransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DocumentRenewalPaymentRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRenewalPaymentRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRenewalPaymentRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void ownedPaymentLookupScopesByRequestTransactionAndMobileUser() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new PaymentTransactionEntity()));

        repository.findOwnedPayment("request-id", "payment-id", "mobile-user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("p.request_id = :requestId"));
        assertTrue(sql.getValue().contains("p.id = :transactionId"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("payment-id", parameters.getValue().getValue("transactionId"));
        assertEquals("mobile-user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void idempotencyLookupScopesByRequestAndMobileUser() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        repository.findByIdempotencyKey("request-id", "retry-key", "mobile-user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("p.request_id = :requestId"));
        assertTrue(sql.getValue().contains("p.idempotency_key = :idempotencyKey"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void webhookProviderChargeLookupUsesForUpdateLock() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new PaymentTransactionEntity()));

        repository.lockByProviderChargeId("chrg_test_1");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("provider = 'OMISE'"));
        assertTrue(sql.getValue().contains("provider_charge_id = :providerChargeId"));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
    }
}
