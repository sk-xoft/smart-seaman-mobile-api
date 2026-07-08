package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.event.DocumentRenewalResubmittedEvent;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentRenewalResubmitService {
    private final DocumentRenewalFoundationRepository repository;
    private final HttpServletRequest httpServletRequest;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DocumentRenewalResubmitResponse resubmit(String requestNoInput) {
        String requestNo = normalizeRequestNo(requestNoInput);
        UsersEntity user = currentUser();
        DocumentRenewalRequestEntity request =
                repository.lockOwnedRequestByNo(requestNo, user.getMobileUuid());
        if (!DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn()
                .equals(request.getStatusNameEn())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentStatus");
        }
        if (repository.countFixItems(request.getId()) == 0) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentRequestItems");
        }
        if (repository.countIncompleteCorrectedFixItems(
                request.getId(), user.getMobileUuid()) > 0) {
            throw new BusinessException(AppStatus.MISSING_PARAMETER, "correctedDocumentItems");
        }

        String targetStatusId = repository.findActiveStatusId(
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW);
        repository.resetFixItemsForReview(request.getId());
        repository.markResubmitted(request.getId());
        repository.updateStatus(request.getId(), request.getDocumentStatusId(), targetStatusId);
        repository.appendTransaction(request.getId(), DocumentRenewalAction.RESUBMIT,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                "Corrected documents resubmitted", user.getMobileUuid());
        eventPublisher.publishEvent(
                new DocumentRenewalResubmittedEvent(user.getMobileUuid(), requestNo));

        return new DocumentRenewalResubmitResponse(requestNo,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.name(),
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.name(),
                DocumentRenewalAction.RESUBMIT.name());
    }

    private UsersEntity currentUser() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user;
    }

    private String normalizeRequestNo(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > 20
                || !value.trim().matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "requestNo");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
