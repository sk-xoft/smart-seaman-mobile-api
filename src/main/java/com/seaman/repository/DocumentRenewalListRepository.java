package com.seaman.repository;

import com.seaman.entity.DocumentRenewalSummaryEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRenewalListRepository extends CommonRepository {
    private static final int PAGE_SIZE = 10;

    public List<DocumentRenewalSummaryEntity> findByUser(String mobileUserUuid, int offSet) {
        String sql = "SELECT r.id AS request_id, r.request_no, r.document_code, "
                + "d.DOCUMENT_NAME_TH AS document_name_th, d.DOCUMENT_NAME_EN AS document_name_en, "
                + "s.id AS status_id, s.document_status_code AS status_code, "
                + "s.name_th AS status_name_th, s.name_en AS status_name_en, "
                + "s.css_color AS status_css_color, "
                + "s.document_mobile_status_code, "
                + "s.document_mobile_status_name_th, "
                + "s.document_mobile_status_name_en, "
                + "r.submitted_at, r.amount, r.is_resubmit "
                + "FROM m_document_request r "
                + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                + "LEFT JOIN m_documents d ON d.DOCUMENT_CODE = "
                + "r.document_code COLLATE utf8mb4_general_ci "
                + "WHERE r.mobile_user_uuid = :mobileUserUuid "
                + "AND r.is_active = 'YES' "
                + "ORDER BY r.submitted_at DESC, r.id DESC LIMIT " + PAGE_SIZE + " OFFSET :offSet";
        return template.query(sql,
                new MapSqlParameterSource().addValue("mobileUserUuid", mobileUserUuid)
                        .addValue("offSet", offSet),
                new BeanPropertyRowMapper<>(DocumentRenewalSummaryEntity.class));
    }

    public int countByUser(String mobileUserUuid) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_document_request "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND is_active = 'YES'",
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid), Integer.class);
        return count == null ? 0 : count;
    }
}
