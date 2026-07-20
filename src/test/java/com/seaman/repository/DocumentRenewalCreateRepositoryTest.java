package com.seaman.repository;

import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemEntity;
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
        assertTrue(sql.getValue().contains("ELSE 'MISSING'"));
    }
}
