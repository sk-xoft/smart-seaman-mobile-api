package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.BusinessConstant;
import com.seaman.entity.ThailandAddressEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ThailandAddressRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Cacheable(cacheNames = BusinessConstant.MASTER_PROVINCES, key = "'all'", sync = true)
    public List<ThailandAddressEntity> findProvinces() {
        String sql = "SELECT code, name_in_thai, name_in_english "
                + "FROM provinces ORDER BY name_in_thai";

        return query(sql, new MapSqlParameterSource());
    }

    public List<ThailandAddressEntity> findDistrictsByProvinceCode(Integer provinceCode) {
        String sql = "SELECT d.code, d.name_in_thai, d.name_in_english "
                + "FROM districts d "
                + "INNER JOIN provinces p ON p.id = d.province_id "
                + "WHERE p.code = :provinceCode "
                + "ORDER BY d.name_in_thai";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("provinceCode", provinceCode);
        return query(sql, parameters);
    }

    public List<ThailandAddressEntity> findSubdistrictsByDistrictCode(Integer districtCode) {
        String sql = "SELECT s.code, s.name_in_thai, s.name_in_english, s.zip_code AS postal_code "
                + "FROM subdistricts s "
                + "INNER JOIN districts d ON d.id = s.district_id "
                + "WHERE d.code = :districtCode "
                + "ORDER BY s.name_in_thai";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("districtCode", districtCode);
        return query(sql, parameters);
    }

    private List<ThailandAddressEntity> query(String sql, MapSqlParameterSource parameters) {
        try {
            return template.query(sql, parameters, new BeanPropertyRowMapper<>(ThailandAddressEntity.class));
        } catch (Exception ex) {
            log.error("Thailand address master query failed: {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public boolean isValidAddress(String province, String district, String subDistrict, String postalCode) {
        String sql = "SELECT COUNT(*) FROM subdistricts s "
                + "INNER JOIN districts d ON d.id = s.district_id "
                + "INNER JOIN provinces p ON p.id = d.province_id "
                + "WHERE (p.name_in_thai = :province OR p.name_in_english = :province) "
                + "AND (d.name_in_thai = :district OR d.name_in_english = :district) "
                + "AND (s.name_in_thai = :subDistrict OR s.name_in_english = :subDistrict) "
                + "AND CAST(s.zip_code AS CHAR) = :postalCode";
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("province", province).addValue("district", district)
                .addValue("subDistrict", subDistrict).addValue("postalCode", postalCode);
        try {
            Integer count = template.queryForObject(sql, parameters, Integer.class);
            return count != null && count > 0;
        } catch (Exception ex) {
            log.error("Thailand address validation failed: {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }
}
