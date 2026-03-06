package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.ListSchoolTrainingEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SchoolTrainingRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<ListSchoolTrainingEntity> listSchoolTrainings(String courseCode) {
        List<ListSchoolTrainingEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from list_course_school_training ");
        sql.append(" where date_format(now(), '%Y%m%d') <= DATE_FOR_CHECK ");
        sql.append(" and COURSE_CODE = :COURSE_CODE ");
        sql.append(" order by  COMPANY_CODE, COURSE_CODE, DATE_FOR_CHECK ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("COURSE_CODE",  courseCode);
            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(ListSchoolTrainingEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<ListSchoolTrainingEntity> schoolTrainingsDetail(String companyCode, String courseCode) {
        List<ListSchoolTrainingEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from list_course_school_training where COMPANY_CODE = :COMPANY_CODE and COURSE_CODE =  :COURSE_CODE ");
        sql.append(" order by  COMPANY_CODE, COURSE_CODE, DATE_FOR_CHECK ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("COMPANY_CODE", companyCode);
            namedParameters.addValue("COURSE_CODE",  courseCode);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(ListSchoolTrainingEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }


}
