package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.DocumentRenewalSummaryEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalSummaryResponse;
import com.seaman.model.response.DocumentRenewalSummaryStatusResponse;
import com.seaman.model.response.PageDocumentRenewalResponse;
import com.seaman.repository.DocumentRenewalListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRenewalListService {
    private static final int PAGE_SIZE = 10;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DocumentRenewalListRepository repository;
    private final HttpServletRequest httpServletRequest;

    public PageDocumentRenewalResponse listMyRequests(int offSet) {
        if (offSet < 0) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "offSet");
        }
        String mobileUserUuid = currentUserUuid();
        int itemTotal = repository.countByUser(mobileUserUuid);
        List<DocumentRenewalSummaryEntity> rows =
                repository.findByUser(mobileUserUuid, offSet);
        List<DocumentRenewalSummaryResponse> items = new ArrayList<>();
        for (DocumentRenewalSummaryEntity row : rows) {
            items.add(map(row));
        }

        PageDocumentRenewalResponse response = new PageDocumentRenewalResponse();
        response.setItemTotal(itemTotal);
        response.setItems(items);
        response.setLast(offSet + rows.size() >= itemTotal || rows.size() < PAGE_SIZE);
        return response;
    }

    private DocumentRenewalSummaryResponse map(DocumentRenewalSummaryEntity row) {
        DocumentRenewalSummaryStatusResponse status = new DocumentRenewalSummaryStatusResponse();
        status.setId(row.getStatusId());
        status.setNameTh(row.getStatusNameTh());
        status.setNameEn(row.getStatusNameEn());
        status.setCssColor(row.getStatusCssColor());
        status.setStep(progressStep(row.getStatusNameEn()));

        DocumentRenewalSummaryResponse response = new DocumentRenewalSummaryResponse();
        response.setRequestId(row.getRequestId());
        response.setRequestNo(row.getRequestNo());
        response.setDocumentCode(row.getDocumentCode());
        response.setDocumentName(documentName(row));
        response.setStatus(status);
        if (row.getSubmittedAt() != null) {
            response.setSubmittedAt(row.getSubmittedAt().toInstant()
                    .atZone(BUSINESS_ZONE).format(DISPLAY_DATE));
        }
        response.setAmount(row.getAmount());
        response.setIsResubmit(Boolean.TRUE.equals(row.getIsResubmit()));
        return response;
    }

    private String documentName(DocumentRenewalSummaryEntity row) {
        String language = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        if ("EN".equalsIgnoreCase(language) && notBlank(row.getDocumentNameEn())) {
            return row.getDocumentNameEn();
        }
        if (notBlank(row.getDocumentNameTh())) {
            return row.getDocumentNameTh();
        }
        return notBlank(row.getDocumentNameEn())
                ? row.getDocumentNameEn() : row.getDocumentCode();
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Integer progressStep(String nameEn) {
        if ("Pending Document Review".equals(nameEn)
                || "Pending Applicant Correction".equals(nameEn)) return 1;
        if ("Pending Marine Department Result".equals(nameEn)) return 2;
        if ("Pending Department Document Pickup".equals(nameEn)) return 3;
        if ("Delivering".equals(nameEn)) return 4;
        if ("Delivered".equals(nameEn)) return 5;
        return null;
    }

    private String currentUserUuid() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user.getMobileUuid();
    }
}
