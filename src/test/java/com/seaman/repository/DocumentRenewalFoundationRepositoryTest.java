package com.seaman.repository;

import com.seaman.entity.RenewalRequestItemEntity;
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
    void locksOwnedItemByRequestNumberAndDocumentItemCode() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(new RenewalRequestItemEntity()));

        repository.lockOwnedRequestItem("260700001", "MRI002", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("r.request_no = :requestNo"));
        assertTrue(sql.getValue().contains(
                "i.document_master_request_item_code = :documentRequestItemCode"));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
        assertEquals("260700001", parameters.getValue().getValue("requestNo"));
        assertEquals("MRI002", parameters.getValue().getValue("documentRequestItemCode"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @Test
    void correctionCompletenessRequiresUpdatedValidProfileFiles() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(0);

        repository.countIncompleteCorrectedFixItems("request-id", "user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Integer.class));
        assertTrue(sql.getValue().contains("i.approve_status = 'FIX'"));
        assertTrue(sql.getValue().contains("p.is_updated = 1"));
        assertTrue(sql.getValue().contains("p.check_result <> 'fix'"));
        assertTrue(sql.getValue().contains("p.slot_code IN ('FRONT','BACK')"));
        assertTrue(sql.getValue().contains("p.document_type = 'PASSPORT'"));
    }
}
