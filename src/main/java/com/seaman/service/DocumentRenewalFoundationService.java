package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalTransitionResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRenewalFoundationService {
    private final DocumentRenewalFoundationRepository repository;
    private final HttpServletRequest httpServletRequest;

    public DocumentRenewalRequestEntity requireOwnedRequest(String requestId) {
        validateRequestId(requestId);
        return repository.findOwnedRequest(requestId, currentUserUuid());
    }

    @Transactional
    public DocumentRenewalTransitionResponse transitionOwnedRequest(
            String requestId, DocumentRenewalStatus expectedFrom,
            DocumentRenewalStatus target, DocumentRenewalAction action, String note) {
        validateRequestId(requestId);
        if (expectedFrom == null || target == null || action == null) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "renewalTransition");
        }
        UsersEntity user = currentUser();
        DocumentRenewalRequestEntity request =
                repository.lockOwnedRequest(requestId, user.getMobileUuid());
        if (!expectedFrom.getMasterNameEn().equals(request.getStatusNameEn())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentStatus");
        }
        String targetStatusId = repository.findActiveStatusId(target);
        repository.updateStatus(requestId, request.getDocumentStatusId(), targetStatusId);
        repository.appendTransaction(requestId, action, expectedFrom, target, note, user.getMobileUuid());
        return new DocumentRenewalTransitionResponse(
                requestId, expectedFrom.name(), target.name(), action.name());
    }

    private String currentUserUuid() {
        return currentUser().getMobileUuid();
    }

    private UsersEntity currentUser() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user;
    }

    private void validateRequestId(String requestId) {
        try {
            UUID.fromString(requestId);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_UUID, "requestId");
        }
    }
}
