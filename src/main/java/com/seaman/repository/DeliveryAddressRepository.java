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

    public void lockUser(String mobileUserUuid) {
        List<String> users = template.query(
                "SELECT MOBILE_UUID FROM m_mobile_users WHERE MOBILE_UUID = :mobileUserUuid FOR UPDATE",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid),
                (rs, rowNum) -> rs.getString("MOBILE_UUID"));
        if (users.size() != 1) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "mobileUser");
        }
    }

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

    public List<DeliveryAddressEntity> findActiveDefaults(String mobileUserUuid) {
        return template.query(
                "SELECT da.*, mu.MOBILE_NUMBER AS mobile_number, "
                        + "CONCAT_WS(' ', da.address_line, "
                        + "CONCAT('ตำบล', sd.name_in_thai), "
                        + "CONCAT('อำเภอ', d.name_in_thai), "
                        + "CONCAT('จังหวัด', p.name_in_thai), da.postal_code) AS description "
                        + "FROM m_delivery_address da "
                        + "INNER JOIN m_mobile_users mu ON mu.MOBILE_UUID = da.mobile_user_uuid "
                        + "LEFT JOIN provinces p ON p.code = da.province "
                        + "LEFT JOIN districts d ON d.code = da.district "
                        + "LEFT JOIN subdistricts sd ON sd.code = da.sub_district "
                        + "WHERE da.mobile_user_uuid = :mobileUserUuid "
                        + "AND da.is_default = 1 AND da.is_active = 'YES' "
                        + "ORDER BY da.updated_at DESC, da.id",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DeliveryAddressEntity.class));
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
