package com.seaman.repository;

import com.seaman.entity.DocumentRenewalSummaryEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRenewalDetailRepository extends CommonRepository {
    public DocumentRenewalSummaryEntity findDocumentNames(String documentCode) {
        List<DocumentRenewalSummaryEntity> rows = template.query(
                "SELECT DOCUMENT_NAME_TH AS document_name_th, "
                        + "DOCUMENT_NAME_EN AS document_name_en FROM m_documents "
                        + "WHERE DOCUMENT_CODE = :documentCode",
                new MapSqlParameterSource("documentCode", documentCode),
                new BeanPropertyRowMapper<>(DocumentRenewalSummaryEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
