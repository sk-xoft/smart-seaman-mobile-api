package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CompanyEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CompanyRepository extends CommonRepository{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_ALL = "select * from m_companys WHERE COMPANY_TYPE = :COMPANY_TYPE AND COMPANY_MOBILE_FLAG = 'Y' AND COMPANY_STATUS = 'A' order by  COMPANY_TYPE , COMPANY_SEQ ";

    public CompanyEntity findByCode(String var) {
        List<CompanyEntity> listAll = null;
        CompanyEntity companyEntity = null;
        StringBuilder sql = new StringBuilder();
        sql.append("select * from m_companys where COMPANY_CODE = :COMPANY_CODE");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("COMPANY_CODE", var);


            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CompanyEntity.class));
            if(!listAll.isEmpty()){
                companyEntity =  listAll.get(0);
            }
            log.info("Init load master company code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return companyEntity;
    }

    public List<CompanyEntity> findAll() {
        List<CompanyEntity> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("COMPANY_TYPE", "Shipping Company");
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(CompanyEntity.class));
            log.info("Init load master company code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }


}
