package com.seaman.repository;

import com.seaman.entity.DocumentRenewalSummaryEntity;
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

class DocumentRenewalListRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DocumentRenewalListRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DocumentRenewalListRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listIsOwnerScopedNewestFirstAndPaginated() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DocumentRenewalSummaryEntity>emptyList());

        repository.findByUser("user-uuid", 20);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("r.mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("r.is_active = 'YES'"));
        assertTrue(sql.getValue().contains("s.document_status_code AS status_code"));
        assertTrue(sql.getValue().contains("r.document_code COLLATE utf8mb4_general_ci"));
        assertTrue(sql.getValue().contains("ORDER BY r.submitted_at DESC, r.id DESC"));
        assertTrue(sql.getValue().contains("LIMIT 10 OFFSET :offSet"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
        assertEquals(20, parameters.getValue().getValue("offSet"));
    }
}
