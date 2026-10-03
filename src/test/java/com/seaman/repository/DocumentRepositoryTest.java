package com.seaman.repository;

import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.DocumentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void missingItemsValidationQueryUsesAggregatedProfileState() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentRequestItemEntity>emptyList());

        repository.findMissingItemsByUserAndDocumentCode("user-uuid", "DOC001");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("FROM m_document_setting_requires dsr"));
        assertTrue(sql.getValue().contains("AS valid_id_front_back_slots"));
        assertTrue(sql.getValue().contains("AS valid_general_main"));
        assertTrue(sql.getValue().contains("AS uploaded_count"));
        assertTrue(sql.getValue().contains("GROUP BY mobile_user_uuid, document_master_request_item_code"));
        assertTrue(sql.getValue().contains("LEFT JOIN m_document_profile_request_item dri"));
        assertFalse(sql.getValue().contains("CAST("));
        assertFalse(sql.getValue().contains("COLLATE"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals("DOC001", parameters.getValue().getValue("documentCode"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void renewalDocumentsQueryJoinsActiveNonTerminalRequests() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentEntity>emptyList());

        repository.findRenewalDocuments();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("select d.document_code, d.DOCUMENT_NAME_TH, d.DOCUMENT_NAME_EN"));
        assertTrue(sql.getValue().contains("ds.name_th as document_renewal_status_name_th"));
        assertTrue(sql.getValue().contains("ds.name_en as document_renewal_status_name_en"));
        assertTrue(sql.getValue().contains("left join m_document_request dr on dr.document_code = d.DOCUMENT_CODE"));
        assertTrue(sql.getValue().contains("left join m_document_status ds on dr.document_status_id = ds.id"));
        assertTrue(sql.getValue().contains("and ds.document_status_code not in ('DELIVERED', 'CANCELLED')"));
        assertTrue(sql.getValue().contains("where DOCUMENT_STATUS = 'A'"));
        assertTrue(sql.getValue().contains("and DOCUMENT_RENEWAL_FLAG in ('Y', 'YES')"));
        assertTrue(sql.getValue().contains("order by DOCUMENT_SEQ"));
    }
}
