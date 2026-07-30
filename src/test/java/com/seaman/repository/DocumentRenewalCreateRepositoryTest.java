package com.seaman.repository;

import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.DeliveryAddressEntity;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentRenewalCreateRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRenewalCreateRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRenewalCreateRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void latestActiveRequestLookupExcludesDeliveredStatus() {
        DocumentRenewalRequestEntity row = new DocumentRenewalRequestEntity();
        row.setId("request-id");
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(row));

        DocumentRenewalRequestEntity result =
                repository.findLatestActiveRequestNotDelivered("user-uuid", "DOC001");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertEquals("request-id", result.getId());
        assertTrue(sql.getValue().contains("s.document_status_code <> 'DELIVERED'"));
        assertTrue(sql.getValue().contains("r.is_active = 'YES'"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("DOC001", parameters.getValue().getValue("documentCode"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void existingRequestItemsQueryReturnsInsertedItemsForValidateResponse() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentRequestItemEntity>emptyList());

        repository.findRequestItemsForValidate("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("FROM m_document_request_items i"));
        assertTrue(sql.getValue().contains("i.request_id = :requestId"));
        assertTrue(sql.getValue().contains("CASE"));
        assertTrue(sql.getValue().contains("LEFT JOIN m_document_profile_request_item p"));
        assertTrue(sql.getValue().contains("p.id AS profile_request_item_id"));
        assertTrue(sql.getValue().contains("WHEN m.storage_scope = 'PROFILE' THEN"));
        assertTrue(sql.getValue().contains("THEN 'NOT_UPLOADED'"));
        assertTrue(sql.getValue().contains("THEN 'INCOMPLETE'"));
        assertTrue(sql.getValue().indexOf("WHEN m.storage_scope = 'PROFILE'")
                < sql.getValue().indexOf("WHEN i.approve_status = 'PASS'"));
        assertTrue(sql.getValue().contains("m_document_profile_request_item pf"));
        assertTrue(sql.getValue().contains("ELSE 'MISSING'"));
    }

    @Test
    void insertRequestSnapshotsUserContactFields() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.insertRequest("request-id", "260700001", "user-uuid",
                "0812345678", "crew@example.com", "DOC001", "status-id",
                "price-setting-id", "address-id", new java.math.BigDecimal("1500.00"),
                "idem-001");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("mobile_number"));
        assertTrue(sql.getValue().contains("email"));
        assertTrue(sql.getValue().contains("idempotency_key"));
        assertEquals("0812345678", parameters.getValue().getValue("mobileNumber"));
        assertEquals("crew@example.com", parameters.getValue().getValue("email"));
        assertEquals("idem-001", parameters.getValue().getValue("idempotencyKey"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void idempotencyLookupScopesByUserAndActiveRequest() {
        DocumentRenewalRequestEntity row = new DocumentRenewalRequestEntity();
        row.setId("request-id");
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(row));

        DocumentRenewalRequestEntity result =
                repository.findByIdempotencyKey("user-uuid", "idem-001");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertEquals("request-id", result.getId());
        assertTrue(sql.getValue().contains("r.idempotency_key = :idempotencyKey"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("r.is_active = 'YES'"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("idem-001", parameters.getValue().getValue("idempotencyKey"));
    }

    @Test
    void insertDeliveryAddressSnapshotCopiesSelectedAddressFields() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        DeliveryAddressEntity address = new DeliveryAddressEntity();
        address.setId("address-id");
        address.setMobileUserUuid("user-uuid");
        address.setFirstName("Somchai");
        address.setLastName("Seaman");
        address.setAddressLine("1 Ocean Road");
        address.setProvince("Bangkok");
        address.setDistrict("Bang Rak");
        address.setSubDistrict("Si Lom");
        address.setPostalCode("10500");

        repository.insertDeliveryAddressSnapshot("request-id", address, "0812345678");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("INSERT INTO m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("source_delivery_address_id"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("address-id", parameters.getValue().getValue("sourceDeliveryAddressId"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("Somchai", parameters.getValue().getValue("firstName"));
        assertEquals("Seaman", parameters.getValue().getValue("lastName"));
        assertEquals("1 Ocean Road", parameters.getValue().getValue("addressLine"));
        assertEquals("Bangkok", parameters.getValue().getValue("province"));
        assertEquals("Bang Rak", parameters.getValue().getValue("district"));
        assertEquals("Si Lom", parameters.getValue().getValue("subDistrict"));
        assertEquals("10500", parameters.getValue().getValue("postalCode"));
        assertEquals("0812345678", parameters.getValue().getValue("mobileNumber"));
    }

    @Test
    void countDeliveryAddressSnapshotScopesByRequestAndOwner() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenReturn(1);

        int count = repository.countDeliveryAddressSnapshot("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).queryForObject(sql.capture(), parameters.capture(), any(Class.class));
        assertEquals(1, count);
        assertTrue(sql.getValue().contains("FROM m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("request_id = :requestId"));
        assertTrue(sql.getValue().contains("mobile_user_uuid = :mobileUserUuid"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void findDeliveryAddressSnapshotUsesRequestAndOwnerScope() {
        DeliveryAddressEntity row = new DeliveryAddressEntity();
        row.setId("address-id");
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(row));

        repository.findDeliveryAddressSnapshot("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("FROM m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("request_id = :requestId"));
        assertTrue(sql.getValue().contains("mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("COALESCE(a.source_delivery_address_id, a.id) AS id"));
        assertTrue(sql.getValue().contains("AS description"));
        assertTrue(sql.getValue().contains("LEFT JOIN provinces p"));
        assertTrue(sql.getValue().contains("LEFT JOIN districts d"));
        assertTrue(sql.getValue().contains("LEFT JOIN subdistricts sd"));
        assertEquals("request-id", parameters.getValue().getValue("requestId"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void findDeliveryAddressSnapshotByIdScopesByOwner() {
        DeliveryAddressEntity row = new DeliveryAddressEntity();
        row.setId("snapshot-id");
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(row));

        DeliveryAddressEntity result =
                repository.findDeliveryAddressSnapshotById("snapshot-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertEquals("snapshot-id", result.getId());
        assertTrue(sql.getValue().contains("FROM m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("a.id = :id"));
        assertTrue(sql.getValue().contains("a.mobile_user_uuid = :mobileUserUuid"));
        assertEquals("snapshot-id", parameters.getValue().getValue("id"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @Test
    void updateDeliveryAddressSnapshotScopesByIdAndOwner() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        DeliveryAddressEntity entity = new DeliveryAddressEntity();
        entity.setId("snapshot-id");
        entity.setMobileUserUuid("user-uuid");
        entity.setFirstName("Somchai");
        entity.setLastName("Seaman");
        entity.setAddressLine("2 Ocean Road");
        entity.setProvince("39");
        entity.setDistrict("3902");
        entity.setSubDistrict("390202");
        entity.setPostalCode("39170");

        repository.updateDeliveryAddressSnapshot(entity);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("UPDATE m_document_request_delivery_address"));
        assertTrue(sql.getValue().contains("WHERE id = :id AND mobile_user_uuid = :mobileUserUuid"));
        assertEquals("snapshot-id", parameters.getValue().getValue("id"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("2 Ocean Road", parameters.getValue().getValue("addressLine"));
    }
}
