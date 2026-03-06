package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.BannerEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class BannerRepository  extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<BannerEntity> findAll() {
        List<BannerEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select BANNER_ID, BANNER_FILE_NAME from m_banners order by CREATE_DATE desc ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(BannerEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public BannerEntity findById(String id) {

        List<BannerEntity> listRecord = null;

        BannerEntity result = null;
        StringBuilder sql = new StringBuilder();
        sql.append("  select BANNER_ID, BANNER_FILE_NAME from m_banners  where BANNER_ID  =  :BANNER_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("BANNER_ID", id);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(BannerEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{bannerId} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}
