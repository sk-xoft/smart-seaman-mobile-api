package com.seaman.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentRequestItemFileRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRequestItemFileRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRequestItemFileRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @Test
    void completenessAllowsIdCardMainOrFrontBackAndRejectsFix() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(1);
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        assertTrue(repository.isComplete("user-id", "MRI001", "ID_CARD"));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Integer.class));
        assertTrue(sql.getValue().contains("slot_code IN ('FRONT','BACK')"));
        assertTrue(sql.getValue().contains("slot_code = 'MAIN'"));
        assertTrue(sql.getValue().contains("check_result <> 'fix'"));
    }

    @Test
    void findProfileItemsQueriesActiveProfileScopeMasterItemsOrderedBySortOrder() {
        when(template.query(anyString(), any(MapSqlParameterSource.class),
                any(org.springframework.jdbc.core.BeanPropertyRowMapper.class))).thenReturn(null);

        repository.findProfileItems("user-id");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class),
                any(org.springframework.jdbc.core.BeanPropertyRowMapper.class));
        assertTrue(sql.getValue().contains("FROM m_document_master_request_item m"));
        assertTrue(sql.getValue().contains("m.is_active = 'YES' AND m.storage_scope = 'PROFILE'"));
        assertTrue(sql.getValue().contains("ORDER BY m.sort_order, m.document_master_items_code"));
        assertTrue(sql.getValue().contains("'MISSING'"));
        assertTrue(sql.getValue().contains("'NOT_UPLOADED'"));
        assertTrue(sql.getValue().contains("'NEED_FIX'"));
        assertTrue(sql.getValue().contains("'COMPLETE'"));
    }
}
