package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.FormEntity;
import com.seaman.entity.ListSchoolTrainingEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FormRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<FormEntity>  findAll() {
        List<FormEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_forms  ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(FormEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public FormEntity  findById(String id) {
        List<FormEntity> listAll = null;
        FormEntity result = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_forms  where FORM_ID = :FORM_ID");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("FORM_ID", id);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(FormEntity.class));
            if(!listAll.isEmpty()){
                result = listAll.get(0);
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }
}
