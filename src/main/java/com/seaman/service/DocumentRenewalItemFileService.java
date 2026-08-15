package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentRenewalItemFileService {
    private final DocumentRenewalFoundationRepository repository;
    private final DocumentRenewalRequestItemFileService requestItemFileService;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public DocumentRequestItemUploadResponse upload(
            String requestNoInput, String documentRequestItemCodeInput, String documentType,
            String slotCode, MultipartFile file) {
        String requestNo = normalize(requestNoInput, "requestNo", 20);
        String documentRequestItemCode = normalize(
                documentRequestItemCodeInput, "documentRequestItemCode", 10);
        RenewalRequestItemEntity item = repository.lockOwnedRequestItem(
                requestNo, documentRequestItemCode, currentUserUuid());
        boolean paymentPending = DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn()
                .equals(item.getStatusNameEn());
        boolean applicantCorrection = DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn()
                .equals(item.getStatusNameEn());
        if (paymentPending) {
            return withRequestContext(item, uploadToRequestScope(item, documentType, slotCode, file));
        }
        if (!applicantCorrection) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentStatus");
        }
        if (!"FIX".equals(item.getApproveStatus())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentRequestItemStatus");
        }
        return withRequestContext(item, uploadToRequestScope(item, documentType, slotCode, file));
    }

    private DocumentRequestItemUploadResponse uploadToRequestScope(
            RenewalRequestItemEntity item, String documentType, String slotCode, MultipartFile file) {
        return requestItemFileService.upload(item.getId(),
                item.getDocumentMasterRequestItemCode(), documentType, slotCode, file);
    }

    private DocumentRequestItemUploadResponse withRequestContext(
            RenewalRequestItemEntity item, DocumentRequestItemUploadResponse response) {
        response.setRequestId(item.getRequestId());
        response.setRequestNo(item.getRequestNo());
        return response;
    }

    private String currentUserUuid() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user.getMobileUuid();
    }

    private String normalize(String value, String field, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength
                || !value.trim().matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, field);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
