package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.NewsEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class NewsRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<NewsEntity> findAll(String type) {
        List<NewsEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_news where NEWS_STATUS = 'A' and NEWS_TYPE = :NEWS_TYPE order by UPDATE_DATE desc ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("NEWS_TYPE", type);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(NewsEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public NewsEntity findById(String id) {

        List<NewsEntity> listRecord = null;

        NewsEntity result = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM m_news where NEWS_ID = :NEWS_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("NEWS_ID", id);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(NewsEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                result = null;
//              throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{newsId} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}