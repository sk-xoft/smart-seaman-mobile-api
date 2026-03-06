package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CourseEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CourseRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<CourseEntity> findAll() {
        List<CourseEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_course_name where COURSE_STATUS = 'A' order by COURSE_SEQ ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(CourseEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }
}
