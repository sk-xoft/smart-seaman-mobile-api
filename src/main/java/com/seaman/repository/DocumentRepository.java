package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.exception.BusinessException;
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
        sql.append("select md.* , mc.CERT_START_DATE , mc.CERT_END_DATE, mc.CERT_FILE, mc.ORIGINAL_FILE_NAME as CERT_FILE_NAME from m_documents md");
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
        sql.append("   dri.id, dri.mobile_user_uuid, dri.document_name, dri.sort_order,");
        sql.append("   dri.file_uploaded, dri.file_path, dri.file_uploaded_at,");
        sql.append("   dri.check_result, dri.check_note, dri.is_updated,");
        sql.append("   dri.checked_at, dri.checked_by, dri.created_at, dri.updated_at,");
        sql.append("   dsr.document_code, dsr.is_required");
        sql.append(" FROM m_document_setting_requires dsr");
        sql.append(" INNER JOIN m_document_request_item dri ON dri.id = dsr.document_items_id");
        sql.append(" WHERE dsr.document_code = :documentCode");
        sql.append("   AND dsr.is_required = 1");
        sql.append("   AND dsr.is_active = 'YES'");
        sql.append("   AND dri.mobile_user_uuid = :mobileUserUuid");
        sql.append("   AND (dri.file_uploaded = 0 OR dri.check_result = 'fix')");
        sql.append(" ORDER BY dri.sort_order");

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
