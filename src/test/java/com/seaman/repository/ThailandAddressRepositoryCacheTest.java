package com.seaman.repository;

import com.seaman.config.CacheConfig;
import com.seaman.entity.ThailandAddressEntity;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThailandAddressRepositoryCacheTest {

    @Test
    void provincesQueryRunsOnceForRepeatedCalls() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                TestConfiguration.class, CacheConfig.class)) {
            ThailandAddressRepository repository = context.getBean(ThailandAddressRepository.class);
            NamedParameterJdbcTemplate template = context.getBean(NamedParameterJdbcTemplate.class);
            ThailandAddressEntity province = new ThailandAddressEntity();
            province.setCode(10);

            when(template.query(anyString(), any(MapSqlParameterSource.class),
                    any(BeanPropertyRowMapper.class))).thenReturn(Collections.singletonList(province));

            assertEquals(1, repository.findProvinces().size());
            assertEquals(1, repository.findProvinces().size());

            verify(template, times(1)).query(anyString(), any(MapSqlParameterSource.class),
                    any(BeanPropertyRowMapper.class));
        }
    }

    @Configuration
    @EnableCaching
    static class TestConfiguration {

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }

        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        ThailandAddressRepository thailandAddressRepository(NamedParameterJdbcTemplate template) {
            ThailandAddressRepository repository = new ThailandAddressRepository();
            repository.template = template;
            return repository;
        }
    }
}
