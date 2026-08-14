package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.BusinessConstant;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class DocumentRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<DocumentEntity> findByType(String docType) {
        List<DocumentEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_documents  where DOCUMENT_TYPE = :DOCUMENT_TYPE  order by DOCUMENT_SEQ ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("DOCUMENT_TYPE", docType);

            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<DocumentEntity> findDefault() {
        List<DocumentEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_documents where DOCUMENT_DEFAULT_FLAG = :DOCUMENT_DEFAULT_FLAG order by DOCUMENT_SEQ ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("DOCUMENT_DEFAULT_FLAG", "Y");

            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<DocumentEntity> findRenewalDocuments() {
        List<DocumentEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_documents ");
        sql.append(" where DOCUMENT_STATUS = 'A' ");
        sql.append(" and DOCUMENT_RENEWAL_FLAG in ('Y', 'YES') ");
        sql.append(" order by DOCUMENT_SEQ ");

        try {
            listAll = template.query(sql.toString(), new MapSqlParameterSource(),
                    new BeanPropertyRowMapper<>(DocumentEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    @Cacheable(cacheNames = BusinessConstant.MASTER_DOCUMENT, key = "#documentCode", sync = true)
    public DocumentEntity findByDocumentCode(String documentCode) {
        List<DocumentEntity> rows;
        try {
            rows = template.query("SELECT * FROM m_documents WHERE DOCUMENT_CODE = :documentCode",
                    new MapSqlParameterSource("documentCode", documentCode),
                    new BeanPropertyRowMapper<>(DocumentEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<DocumentEntity> findByPage(String userUid, int offSet,  String documentType) {
        List<DocumentEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append("select md.* , mc.CERT_START_DATE , mc.CERT_END_DATE, mc.CERT_FILE, mc.ORIGINAL_FILE_NAME as CERT_FILE_NAME from m_documents md");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A'");
        sql.append(" and md.DOCUMENT_TYPE = :documentType ");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" order by DOCUMENT_SEQ");
        sql.append(" LIMIT 10 OFFSET :offSet ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()

            .addValue("documentType", documentType)
            .addValue("userId", userUid)
            .addValue("offSet", offSet);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<DocumentEntity> findCloseToExpiration(String userUid, int offSet) {
        List<DocumentEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append("select md.* , mc.CERT_START_DATE , mc.CERT_END_DATE, mc.CERT_FILE, mc.ORIGINAL_FILE_NAME as CERT_FILE_NAME, ");
        sql.append(" case when exists ( ");
        sql.append(" select 1 from m_document_request dr ");
        sql.append(" inner join m_document_status ds on ds.id = dr.document_status_id ");
        sql.append(" where dr.mobile_user_uuid = :userId ");
        sql.append(" and dr.document_code = md.DOCUMENT_CODE COLLATE utf8mb4_general_ci ");
        sql.append(" and dr.is_active = 'YES' ");
        sql.append(" and ds.document_status_code not in ('DELIVERED', 'CANCELLED') ");
        sql.append(" ) then 'Y' else 'N' end as DOCUMENT_RENEWAL_PROCESSING_FLAG ");
        sql.append(" from m_documents md");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A'");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" and CERT_END_DATE <= NOW() + INTERVAL 18 MONTH ");
        sql.append(" order by CERT_END_DATE, DOCUMENT_SEQ ");
        sql.append(" LIMIT 10 OFFSET :offSet ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("userId", userUid)
                    .addValue("offSet", offSet);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<DocumentEntity> findCloseToExpiration18Month() {

        List<DocumentEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append("select md.* , mc.CERT_START_DATE , mc.CERT_END_DATE, mc.CERT_FILE, mc.ORIGINAL_FILE_NAME as CERT_FILE_NAME from m_documents md");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A'");
        sql.append(" and CERT_END_DATE <= NOW() + INTERVAL 18 MONTH ");
        sql.append(" order by CERT_END_DATE, DOCUMENT_SEQ ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public int countByPageByUserUidCloseToExpiration(String userUid) {

        int row = 0;

        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) from m_documents md ");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE ");
        sql.append(" where ");
        sql.append(" md.DOCUMENT_STATUS = 'A' ");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" and CERT_END_DATE <= NOW() + INTERVAL 18 MONTH ");
        sql.append(" order by CERT_END_DATE, DOCUMENT_SEQ ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("userId", userUid);

            row = template.queryForObject(sql.toString(), namedParameters, Integer.class);

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return row;
    }

    public int countByPageByUserUid(String userUid, String documentType) {

        int row = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from m_documents md");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A'");
        sql.append(" and md.DOCUMENT_TYPE = :documentType ");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" order by DOCUMENT_SEQ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("documentType", documentType)
                    .addValue("userId", userUid);

            row = template.queryForObject(sql.toString(), namedParameters, Integer.class);

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return row;
    }

    public List<DocumentRequestItemEntity> findMissingItemsByUserAndDocumentCode(
            String mobileUserUuid, String documentCode) {

        List<DocumentRequestItemEntity> result = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append("   dri.id, dri.id AS profile_request_item_id, dri.mobile_user_uuid,");
        sql.append("   dsr.document_master_request_item_code, dmri.storage_scope,");
        sql.append("   dri.document_type, dmri.document_master_items_name AS document_name,");
        sql.append("   CASE");
        sql.append("     WHEN dmri.storage_scope = 'REQUEST' THEN 'MISSING'");
        sql.append("     WHEN dsr.document_master_request_item_code = 'MRI001' AND (");
        sql.append("       COALESCE(ps.valid_id_front_back_slots, 0) = 2");
        sql.append("       OR COALESCE(ps.valid_id_main, 0) > 0");
        sql.append("       OR COALESCE(ps.valid_passport_main, 0) > 0)");
        sql.append("       THEN 'COMPLETE'");
        sql.append("     WHEN dsr.document_master_request_item_code <> 'MRI001'");
        sql.append("       AND COALESCE(ps.valid_general_main, 0) > 0");
        sql.append("       THEN 'COMPLETE'");
        sql.append("     WHEN COALESCE(ps.fix_count, 0) > 0 THEN 'NEED_FIX'");
        sql.append("     WHEN dri.id IS NULL THEN 'MISSING'");
        sql.append("     WHEN COALESCE(ps.uploaded_count, 0) = 0 THEN 'NOT_UPLOADED'");
        sql.append("     ELSE 'INCOMPLETE'");
        sql.append("   END AS document_status,");
        sql.append("   dsr.sort_order,");
        sql.append("   CASE WHEN COALESCE(ps.uploaded_count, 0) > 0 THEN 1 ELSE 0 END AS file_uploaded,");
        sql.append("   dri.file_path, dri.file_uploaded_at,");
        sql.append("   dri.check_result, dri.check_note, dri.is_updated,");
        sql.append("   dri.checked_at, dri.checked_by, dri.created_at, dri.updated_at,");
        sql.append("   dsr.document_code, dsr.is_required");
        sql.append(" FROM m_document_setting_requires dsr");
        sql.append(" INNER JOIN m_document_master_request_item dmri");
        sql.append("   ON dmri.document_master_items_code = dsr.document_master_request_item_code");
        sql.append(" LEFT JOIN (");
        sql.append("   SELECT mobile_user_uuid, document_master_request_item_code, MIN(id) AS profile_item_id");
        sql.append("   FROM m_document_profile_request_item");
        sql.append("   WHERE mobile_user_uuid = :mobileUserUuid");
        sql.append("   GROUP BY mobile_user_uuid, document_master_request_item_code");
        sql.append(" ) pmin ON pmin.mobile_user_uuid = :mobileUserUuid");
        sql.append("   AND pmin.document_master_request_item_code = dsr.document_master_request_item_code");
        sql.append(" LEFT JOIN m_document_profile_request_item dri");
        sql.append("   ON dri.id = pmin.profile_item_id");
        sql.append(" LEFT JOIN (");
        sql.append("   SELECT mobile_user_uuid, document_master_request_item_code,");
        sql.append("     COUNT(DISTINCT CASE WHEN document_type = 'ID_CARD'");
        sql.append("       AND slot_code IN ('FRONT','BACK') AND file_uploaded = 1");
        sql.append("       AND (check_result IS NULL OR check_result <> 'fix') THEN slot_code END)");
        sql.append("       AS valid_id_front_back_slots,");
        sql.append("     SUM(CASE WHEN document_type = 'ID_CARD' AND slot_code = 'MAIN'");
        sql.append("       AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix')");
        sql.append("       THEN 1 ELSE 0 END) AS valid_id_main,");
        sql.append("     SUM(CASE WHEN document_type = 'PASSPORT' AND slot_code = 'MAIN'");
        sql.append("       AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix')");
        sql.append("       THEN 1 ELSE 0 END) AS valid_passport_main,");
        sql.append("     SUM(CASE WHEN document_type = 'GENERAL' AND slot_code = 'MAIN'");
        sql.append("       AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix')");
        sql.append("       THEN 1 ELSE 0 END) AS valid_general_main,");
        sql.append("     SUM(CASE WHEN check_result = 'fix' THEN 1 ELSE 0 END) AS fix_count,");
        sql.append("     SUM(CASE WHEN file_uploaded = 1 THEN 1 ELSE 0 END) AS uploaded_count");
        sql.append("   FROM m_document_profile_request_item");
        sql.append("   WHERE mobile_user_uuid = :mobileUserUuid");
        sql.append("   GROUP BY mobile_user_uuid, document_master_request_item_code");
        sql.append(" ) ps ON ps.mobile_user_uuid = :mobileUserUuid");
        sql.append("   AND ps.document_master_request_item_code = dsr.document_master_request_item_code");
        sql.append(" WHERE dsr.document_code = :documentCode");
        sql.append("   AND dsr.is_required = 1");
        sql.append("   AND dsr.is_active = 'YES'");
        sql.append("   AND dmri.is_active = 'YES'");
        sql.append(" ORDER BY dsr.sort_order, dmri.sort_order");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("documentCode", documentCode)
                    .addValue("mobileUserUuid", mobileUserUuid);

            result = template.query(sql.toString(), namedParameters,
                    new BeanPropertyRowMapper<>(DocumentRequestItemEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}
