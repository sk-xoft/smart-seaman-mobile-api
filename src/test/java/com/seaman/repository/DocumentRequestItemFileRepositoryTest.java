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
    void completenessChecksEveryRequiredSlotAndRejectedFile() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(1);
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        assertTrue(!repository.isComplete("user-id", "MRI001", "ID_CARD"));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Integer.class));
        assertTrue(sql.getValue().contains("slot_code IN ('FRONT','BACK')"));
        assertTrue(sql.getValue().contains("check_result <> 'fix'"));
    }
}
