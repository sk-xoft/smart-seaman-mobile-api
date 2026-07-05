package com.seaman.repository;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeliveryAddressRepositoryTest {
    private NamedParameterJdbcTemplate template;
    private DeliveryAddressRepository repository;

    @BeforeEach
    void setUp() {
        template = mock(NamedParameterJdbcTemplate.class);
        repository = new DeliveryAddressRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @SuppressWarnings("unchecked")
    @Test
    void defaultQueryFiltersByOwnerActiveAndDefault() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.<DeliveryAddressEntity>emptyList());

        repository.findActiveDefaults("user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).query(sql.capture(), parameters.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("is_default = 1"));
        assertTrue(sql.getValue().contains("is_active = 'YES'"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void locksMobileUserRowBeforeAddressMutation() {
        when(template.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList("user-uuid"));
        repository.lockUser("user-uuid");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).query(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("m_mobile_users"));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
    }

    @Test
    void insertUsesServerOwnedIdentityAndActiveFlag() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        DeliveryAddressEntity entity = new DeliveryAddressEntity();
        entity.setId("address-id");
        entity.setMobileUserUuid("user-uuid");
        entity.setIsDefault(true);
        repository.insert(entity);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> parameters =
                ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(template).update(sql.capture(), parameters.capture());
        assertTrue(sql.getValue().contains("'YES'"));
        assertEquals("user-uuid", parameters.getValue().getValue("mobileUserUuid"));
    }

    @Test
    void updateFiltersByAddressOwnerAndActiveFlag() {
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        DeliveryAddressEntity entity = new DeliveryAddressEntity();
        entity.setId("address-id");
        entity.setMobileUserUuid("user-uuid");
        entity.setIsDefault(false);
        repository.update(entity);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(template).update(sql.capture(), any(MapSqlParameterSource.class));
        assertTrue(sql.getValue().contains("id = :id"));
        assertTrue(sql.getValue().contains("mobile_user_uuid = :mobileUserUuid"));
        assertTrue(sql.getValue().contains("is_active = 'YES'"));
        assertTrue(sql.getValue().contains("updated_at = NOW()"));
    }
}
