package com.seaman.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThailandAddressRepositoryValidationTest {
    private NamedParameterJdbcTemplate template;
    private ThailandAddressRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new ThailandAddressRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @Test
    void validatesAddressUsingPublicMasterCodes() {
        when(template.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
                .thenReturn(1);

        assertTrue(repository.isValidAddress("39", "3902", "390202", "39170"));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).queryForObject(sql.capture(), parameters.capture(), eq(Integer.class));
        assertTrue(sql.getValue().contains("p.code = :provinceCode"));
        assertTrue(sql.getValue().contains("d.code = :districtCode"));
        assertTrue(sql.getValue().contains("s.code = :subDistrictCode"));
        assertEquals("39", parameters.getValue().getValue("provinceCode"));
        assertEquals("3902", parameters.getValue().getValue("districtCode"));
        assertEquals("390202", parameters.getValue().getValue("subDistrictCode"));
        assertEquals("39170", parameters.getValue().getValue("postalCode"));
    }
}
