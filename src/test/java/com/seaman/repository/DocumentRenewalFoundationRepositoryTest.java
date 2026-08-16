package com.seaman.repository;

import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalTransactionEntity;
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

class DocumentRenewalFoundationRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRenewalFoundationRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRenewalFoundationRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void activeStatusLookupUsesStatusCode() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList("status-id"));

        String result = repository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertEquals("status-id", result);
        assertTrue(sql.getValue().contains("document_status_code = :statusCode"));
        assertEquals("PAYMENT_PENDING", parameters.getValue().getValue("statusCode"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void locksOwnedItemByRequestNumberAndDocumentItemCode() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new RenewalRequestItemEntity()));

        repository.lockOwnedRequestItem("260700001", "MRI002", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("r.request_no = :requestNo"));
        assertTrue(sql.getValue().contains("r.is_active = 'YES'"));
        assertTrue(sql.getValue().contains(
                "i.document_master_request_item_code = :documentRequestItemCode"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
        assertEquals("260700001", parameters.getValue().getValue("requestNo"));
        assertEquals("MRI002", parameters.getValue().getValue("documentRequestItemCode"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void findsOwnedItemByRequestNumberAndDocumentItemCodeWithoutLock() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new RenewalRequestItemEntity()));

        repository.findOwnedRequestItem("260700001", "MRI002", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("r.request_no = :requestNo"));
        assertTrue(sql.getValue().contains(
                "i.document_master_request_item_code = :documentRequestItemCode"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("m_document_request_items i"));
        assertTrue(!sql.getValue().contains("FOR UPDATE"));
        assertEquals("260700001", parameters.getValue().getValue("requestNo"));
        assertEquals("MRI002", parameters.getValue().getValue("documentRequestItemCode"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @Test
    void correctionCompletenessRequiresUpdatedValidRequestScopedFilesRegardlessOfStorageScope() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        repository.countIncompleteCorrectedFixItems("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Integer.class));
        assertTrue(sql.getValue().contains("i.approve_status = 'FIX'"));
        assertTrue(sql.getValue().contains("f.is_updated = 1"));
        assertTrue(sql.getValue().contains("f.check_result <> 'fix'"));
        assertTrue(sql.getValue().contains("f.slot_code IN ('FRONT','BACK')"));
        assertTrue(sql.getValue().contains("f.slot_code = 'MAIN'"));
        assertTrue(sql.getValue().contains("f.document_type = 'PASSPORT'"));
        assertTrue(sql.getValue().contains("f.request_item_id = i.id"));
        assertTrue(sql.getValue().contains("m_document_request_item_files f"));
        assertTrue(!sql.getValue().contains("m_document_profile_request_item"));
        assertTrue(!sql.getValue().contains("m.storage_scope"));
    }

    @Test
    void requestScopedPaymentReadinessRequiresPerRequestFiles() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        repository.countIncompleteRequestScopedItems("request-id");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Integer.class));
        assertTrue(sql.getValue().contains("m.storage_scope = 'REQUEST'"));
        assertTrue(sql.getValue().contains("m_document_request_item_files f"));
        assertTrue(sql.getValue().contains("f.slot_code IN ('FRONT','BACK')"));
        assertTrue(sql.getValue().contains("f.slot_code = 'MAIN'"));
        assertTrue(sql.getValue().contains("f.document_type = 'PASSPORT'"));
        assertTrue(sql.getValue().contains("f.document_type = 'GENERAL'"));
    }

    @Test
    void updatesRequestMobileNumberByActiveRequestId() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.updateRequestMobileNumber("request-id", "0821549970");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("UPDATE m_document_request"));
        assertTrue(sql.getValue().contains("mobile_number = :mobileNumber"));
        assertTrue(sql.getValue().contains("id = :requestId"));
        assertTrue(sql.getValue().contains("is_active = 'YES'"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("0821549970", parameters.getValue().getValue("mobileNumber"));
    }

    @Test
    void updatesDeliveryAddressSnapshotMobileNumberByRequestAndOwner() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.updateDeliveryAddressMobileNumber("request-id", "user-uuid", "0821549970");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("UPDATE m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("mobile_number = :mobileNumber"));
        assertTrue(sql.getValue().contains("request_id = :requestId"));
        assertTrue(sql.getValue().contains("mobile_user_uuid = :mobileUserUuid"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("0821549970", parameters.getValue().getValue("mobileNumber"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void timelineOwnershipLookupDoesNotLockAndRowsAreDeterministicallyOrdered() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new DocumentRenewalRequestEntity()))
                .thenReturn(Collections.<DocumentRenewalTransactionEntity>emptyList());

        repository.findOwnedRequestByNo("260700001", "user-uuid");
        repository.findOwnedTransactions("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template, times(2)).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getAllValues().get(0).contains("r.request_no = :requestNo"));
        assertTrue(sql.getAllValues().get(0).contains("r.is_active = 'YES'"));
        assertTrue(!sql.getAllValues().get(0).contains("FOR UPDATE"));
        assertTrue(sql.getAllValues().get(1).contains("fs.document_mobile_status_name_th"));
        assertTrue(sql.getAllValues().get(1).contains("fs.document_mobile_status_name_en"));
        assertTrue(sql.getAllValues().get(1).contains("ts.document_mobile_status_name_th"));
        assertTrue(sql.getAllValues().get(1).contains("ts.document_mobile_status_name_en"));
        assertTrue(sql.getAllValues().get(1).contains("LEFT JOIN m_document_status fs"));
        assertTrue(sql.getAllValues().get(1).contains("LEFT JOIN m_document_status ts"));
        assertTrue(sql.getAllValues().get(1).contains("ORDER BY t.actioned_at, t.id"));
        assertTrue(!sql.getAllValues().get(1).contains("document_mobile_status_code"));
        assertTrue(!sql.getAllValues().get(1).contains("COALESCE"));
    }
}
