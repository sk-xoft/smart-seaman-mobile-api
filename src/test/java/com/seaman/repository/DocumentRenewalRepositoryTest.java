package com.seaman.repository;

import com.seaman.entity.DocumentRenewalPriceEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DocumentRenewalRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRenewalRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRenewalRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void activeStatusQuerySelectsAndOrdersByStatusCode() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentRenewalStatusEntity>emptyList());

        repository.findActiveStatuses();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("document_status_code"));
        assertTrue(sql.getValue().contains("document_mobile_status_code"));
        assertTrue(sql.getValue().contains("document_mobile_status_name_th"));
        assertTrue(sql.getValue().contains("document_mobile_status_name_en"));
        assertTrue(sql.getValue().contains("is_mobile_visible = 'YES'"));
        assertTrue(sql.getValue().contains("ORDER BY CASE document_status_code"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void activePriceQueryAppliesEffectivePeriodAndActiveDocument() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentRenewalPriceEntity>emptyList());

        repository.findActivePrices("DOC001");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("p.effective_from <= CURRENT_DATE"));
        assertTrue(sql.getValue().contains("p.effective_to IS NULL OR p.effective_to >= CURRENT_DATE"));
        assertTrue(sql.getValue().contains("p.is_active = 'YES'"));
        assertTrue(sql.getValue().contains("d.DOCUMENT_STATUS = 'A'"));
    }
}
