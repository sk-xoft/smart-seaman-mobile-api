package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.ForgotPasswordEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ForgotPasswordRepository extends CommonRepository{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<ForgotPasswordEntity> findByUid(String var1) {
        List<ForgotPasswordEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select  id, user_uuid, is_status, created_at from t_forgot_password where IS_STATUS = 'NO' and USER_UUID  = :USER_UUID  ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("USER_UUID",  var1);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(ForgotPasswordEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public boolean insert(String userUid) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  insert into t_forgot_password (USER_UUID, IS_STATUS, CREATED_AT) VALUE (:USER_UUID, 'NO', now())  ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_UUID", userUid);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean delete(String userUid) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  delete from t_forgot_password where USER_UUID = :USER_UUID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_UUID", userUid);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean update(String userUid) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append("  update t_forgot_password set IS_STATUS = 'YES', CREATED_AT = now() where USER_UUID = :USER_UUID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_UUID", userUid);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}