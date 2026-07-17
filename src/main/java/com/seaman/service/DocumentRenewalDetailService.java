package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DeliveryEntity;
import com.seaman.entity.DeptSubmissionEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalSummaryEntity;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalDeliveryResponse;
import com.seaman.model.response.DocumentRenewalDeptSubmissionResponse;
import com.seaman.model.response.DocumentRenewalDetailFileResponse;
import com.seaman.model.response.DocumentRenewalDetailItemResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.model.response.DocumentRenewalSummaryStatusResponse;
import com.seaman.repository.DocumentRenewalDetailRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalRequestItemFileRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentRenewalDetailService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final long SIGNED_URL_TTL_MILLIS = 10L * 60 * 1000;

    private final DocumentRenewalFoundationRepository foundationRepository;
    private final DocumentRenewalDetailRepository detailRepository;
    private final DocumentRequestItemFileRepository fileRepository;
    private final DocumentRenewalRequestItemFileRepository requestItemFileRepository;
    private final AmazonS3 s3;
    private final HttpServletRequest httpServletRequest;

    @Value("${object.store.bucket}")
    private String bucketName;

    public DocumentRenewalDetailResponse detail(String requestNoInput) {
        String requestNo = normalizeRequestNo(requestNoInput);
        String userUuid = currentUserUuid();
        DocumentRenewalRequestEntity request =
                foundationRepository.findOwnedRequestByNo(requestNo, userUuid);
        DocumentRenewalSummaryEntity document =
                detailRepository.findDocumentNames(request.getDocumentCode());

        DocumentRenewalDetailResponse response = new DocumentRenewalDetailResponse();
        response.setRequestId(request.getId());
        response.setRequestNo(request.getRequestNo());
        response.setMobileUserUuid(request.getMobileUserUuid());
        response.setDocumentCode(request.getDocumentCode());
        response.setDocumentName(documentName(document, request.getDocumentCode()));
        response.setStatus(status(request));
        response.setSubmittedAt(formatDateTime(request.getSubmittedAt()));
        response.setAmount(request.getAmount());
        response.setIsResubmit(Boolean.TRUE.equals(request.getIsResubmit()));
        response.setItems(items(request.getId(), userUuid));

        if (showsDepartmentDetail(request.getStatusNameEn())) {
            List<DeptSubmissionEntity> rows = foundationRepository.findOwnedDeptSubmissions(
                    request.getId(), userUuid);
            requireAtMostOne(rows.size(), "documentRenewalDepartmentSubmission");
            if (!rows.isEmpty()) response.setDeptSubmission(department(rows.get(0)));
        }
        if (showsDeliveryDetail(request.getStatusNameEn())) {
            List<DeliveryEntity> rows = foundationRepository.findOwnedDeliveries(
                    request.getId(), userUuid);
            requireAtMostOne(rows.size(), "documentRenewalDelivery");
            if (!rows.isEmpty()) response.setDelivery(delivery(rows.get(0)));
        }
        return response;
    }

    private List<DocumentRenewalDetailItemResponse> items(String requestId, String userUuid) {
        List<RenewalRequestItemEntity> rows =
                foundationRepository.findOwnedRequestItems(requestId, userUuid);
        List<DocumentRenewalDetailItemResponse> result = new ArrayList<>();
        for (RenewalRequestItemEntity row : rows) {
            List<DocumentRequestItemFileEntity> files = "REQUEST".equals(row.getStorageScope())
                    ? requestItemFileRepository.findFiles(row.getId())
                    : fileRepository.findFiles(userUuid, row.getDocumentMasterRequestItemCode());
            List<DocumentRenewalDetailFileResponse> mappedFiles = new ArrayList<>();
            boolean updated = false;
            boolean fileUploaded = false;
            for (DocumentRequestItemFileEntity file : files) {
                mappedFiles.add(file(file));
                updated = updated || Boolean.TRUE.equals(file.getIsUpdated());
                fileUploaded = fileUploaded || Integer.valueOf(1).equals(file.getFileUploaded());
            }
            DocumentRenewalDetailItemResponse item = new DocumentRenewalDetailItemResponse();
            item.setItemId(row.getId());
            item.setDocumentRequestItemCode(row.getDocumentMasterRequestItemCode());
            item.setStorageScope(row.getStorageScope());
            item.setDocumentName(itemName(row));
            item.setSortOrder(row.getSortOrder());
            item.setFileUploaded(fileUploaded);
            item.setCheckResult(checkResult(row.getApproveStatus()));
            if ("FIX".equals(row.getApproveStatus())) item.setCheckNote(row.getNote());
            item.setIsUpdated(updated);
            item.setFiles(mappedFiles);
            result.add(item);
        }
        return result;
    }

    private DocumentRenewalDetailFileResponse file(DocumentRequestItemFileEntity row) {
        DocumentRenewalDetailFileResponse response = new DocumentRenewalDetailFileResponse();
        response.setFileId(row.getId());
        response.setDocumentType(row.getDocumentType());
        response.setSlotCode(row.getSlotCode());
        response.setOriginalFileName(row.getOriginalFileName());
        response.setMimeType(row.getMimeType());
        response.setFileSize(row.getFileSize());
        response.setFileUploadedAt(formatDateTime(row.getFileUploadedAt()));
        response.setIsUpdated(Boolean.TRUE.equals(row.getIsUpdated()));
        if (Integer.valueOf(1).equals(row.getFileUploaded())
                && row.getStorageKey() != null && !row.getStorageKey().trim().isEmpty()) {
            Date expiresAt = new Date(System.currentTimeMillis() + SIGNED_URL_TTL_MILLIS);
            response.setFileUrl(s3.generatePresignedUrl(
                    bucketName, row.getStorageKey(), expiresAt).toString());
        }
        return response;
    }

    private DocumentRenewalSummaryStatusResponse status(DocumentRenewalRequestEntity request) {
        DocumentRenewalSummaryStatusResponse status =
                new DocumentRenewalSummaryStatusResponse();
        status.setId(request.getDocumentStatusId());
        status.setNameTh(request.getStatusNameTh());
        status.setNameEn(request.getStatusNameEn());
        status.setCssColor(request.getStatusCssColor());
        status.setStep(progressStep(request.getStatusNameEn()));
        return status;
    }

    private DocumentRenewalDeptSubmissionResponse department(DeptSubmissionEntity row) {
        DocumentRenewalDeptSubmissionResponse response =
                new DocumentRenewalDeptSubmissionResponse();
        if (row.getSubmittedToDeptDate() != null)
            response.setSubmittedToDeptDate(row.getSubmittedToDeptDate().format(DISPLAY_DAY));
        if (row.getAvailableFromDate() != null)
            response.setAvailableFromDate(row.getAvailableFromDate().format(DISPLAY_DAY));
        if (row.getReceivedFromDeptDate() != null)
            response.setReceivedFromDeptDate(row.getReceivedFromDeptDate().format(DISPLAY_DAY));
        response.setRecordedAt(formatDateTime(row.getRecordedAt()));
        return response;
    }

    private DocumentRenewalDeliveryResponse delivery(DeliveryEntity row) {
        DocumentRenewalDeliveryResponse response = new DocumentRenewalDeliveryResponse();
        response.setTrackingNo(row.getTrackingNo());
        response.setCarrier(row.getCarrier());
        if (row.getShippedDate() != null)
            response.setShippedDate(row.getShippedDate().format(DISPLAY_DAY));
        response.setDeliveryStatus(row.getDeliveryStatus());
        response.setShippedRecordedAt(formatDateTime(row.getShippedRecordedAt()));
        response.setDeliveredAt(formatDateTime(row.getDeliveredAt()));
        return response;
    }

    private boolean showsDepartmentDetail(String status) {
        return DocumentRenewalStatus.PENDING_MARINE_DEPARTMENT_RESULT.getMasterNameEn().equals(status)
                || DocumentRenewalStatus.PENDING_DEPARTMENT_DOCUMENT_PICKUP.getMasterNameEn().equals(status)
                || showsDeliveryDetail(status);
    }

    private boolean showsDeliveryDetail(String status) {
        return DocumentRenewalStatus.DELIVERING.getMasterNameEn().equals(status)
                || DocumentRenewalStatus.DELIVERED.getMasterNameEn().equals(status);
    }

    private String checkResult(String status) {
        if ("FIX".equals(status)) return "fix";
        if ("PASS".equals(status)) return "pass";
        return null;
    }

    private String itemName(RenewalRequestItemEntity row) {
        if (isEnglish() && notBlank(row.getDocumentNameEn())) return row.getDocumentNameEn();
        if (notBlank(row.getDocumentNameTh())) return row.getDocumentNameTh();
        return row.getDocumentNameEn();
    }

    private String documentName(DocumentRenewalSummaryEntity row, String fallback) {
        if (row == null) return fallback;
        if (isEnglish() && notBlank(row.getDocumentNameEn())) return row.getDocumentNameEn();
        if (notBlank(row.getDocumentNameTh())) return row.getDocumentNameTh();
        return notBlank(row.getDocumentNameEn()) ? row.getDocumentNameEn() : fallback;
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

    private String formatDateTime(Date value) {
        return value == null ? null
                : value.toInstant().atZone(BUSINESS_ZONE).format(DISPLAY_DATE);
    }

    private boolean isEnglish() {
        return "EN".equalsIgnoreCase(
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void requireAtMostOne(int size, String field) {
        if (size > 1) throw new BusinessException(AppStatus.EXCEPTION_DATABASE, field);
    }

    private String currentUserUuid() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user.getMobileUuid();
    }

    private String normalizeRequestNo(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > 20
                || !value.trim().matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "requestNo");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
