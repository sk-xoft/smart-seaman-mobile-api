package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DeliveryAddressRepository extends CommonRepository {

    public void lockActiveAddresses(String mobileUserUuid) {
        template.query("SELECT id FROM m_delivery_address "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND is_active = 'YES' FOR UPDATE",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid),
                (rs, rowNum) -> rs.getString("id"));
    }

    public int countActive(String mobileUserUuid) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_delivery_address "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND is_active = 'YES'",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid), Integer.class);
        return count == null ? 0 : count;
    }

    public void clearDefault(String mobileUserUuid) {
        template.update("UPDATE m_delivery_address SET is_default = 0, updated_at = NOW() "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND is_active = 'YES' AND is_default = 1",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid));
    }

    public void insert(DeliveryAddressEntity entity) {
        String sql = "INSERT INTO m_delivery_address "
                + "(id, mobile_user_uuid, first_name, last_name, address_line, province, district, "
                + "sub_district, postal_code, is_default, is_active) VALUES "
                + "(:id, :mobileUserUuid, :firstName, :lastName, :addressLine, :province, :district, "
                + ":subDistrict, :postalCode, :isDefault, 'YES')";
        if (template.update(sql, parameters(entity)) != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "deliveryAddress");
        }
    }

    public DeliveryAddressEntity findActiveOwned(String id, String mobileUserUuid) {
        List<DeliveryAddressEntity> rows = template.query(
                "SELECT * FROM m_delivery_address WHERE id = :id "
                        + "AND mobile_user_uuid = :mobileUserUuid AND is_active = 'YES'",
                new MapSqlParameterSource().addValue("id", id)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DeliveryAddressEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void update(DeliveryAddressEntity entity) {
        String sql = "UPDATE m_delivery_address SET first_name = :firstName, last_name = :lastName, "
                + "address_line = :addressLine, province = :province, district = :district, "
                + "sub_district = :subDistrict, postal_code = :postalCode, is_default = :isDefault, "
                + "updated_at = NOW() WHERE id = :id AND mobile_user_uuid = :mobileUserUuid "
                + "AND is_active = 'YES'";
        if (template.update(sql, parameters(entity)) != 1) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "deliveryAddress");
        }
    }

    private MapSqlParameterSource parameters(DeliveryAddressEntity entity) {
        return new MapSqlParameterSource()
                .addValue("id", entity.getId()).addValue("mobileUserUuid", entity.getMobileUserUuid())
                .addValue("firstName", entity.getFirstName()).addValue("lastName", entity.getLastName())
                .addValue("addressLine", entity.getAddressLine()).addValue("province", entity.getProvince())
                .addValue("district", entity.getDistrict()).addValue("subDistrict", entity.getSubDistrict())
                .addValue("postalCode", entity.getPostalCode()).addValue("isDefault", entity.getIsDefault());
    }
}
