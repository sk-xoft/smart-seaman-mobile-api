package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.VoucherEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VoucherRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<VoucherEntity> findAll(String smartSeamanId) {
        List<VoucherEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append("  select * from (select mv.* ");
        sql.append("                   from m_vouchers mv ");
        sql.append("                            inner join m_voucher_details mvd on mv.VOUCHER_ID = mvd.VOUCHER_ID ");
        sql.append("                   where  VOUCHER_STATUS = 'A' and VOUCHER_TYPE = 'PERSONAL' ");
        sql.append("                     and mvd.SMART_SEAMAN_ID = :SMART_SEAMAN_ID ");
        sql.append("                   union all ");
        sql.append("                   select mv.* ");
        sql.append("                   from m_vouchers mv ");
        sql.append("                   where VOUCHER_STATUS = 'A' and VOUCHER_TYPE = 'GLOBAL') as tbl_a order by CREATE_DATE desc ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("SMART_SEAMAN_ID", smartSeamanId);
            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(VoucherEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public VoucherEntity findById(String id) {

        List<VoucherEntity> listRecord = null;

        VoucherEntity result = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_vouchers where VOUCHER_STATUS = 'A' and VOUCHER_ID = :VOUCHER_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", id);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(VoucherEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                result = null;
                // throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{voucher_id} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}