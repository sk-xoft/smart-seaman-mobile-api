package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalDeleteResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentRenewalHardDeleteService {
    private final DocumentRenewalFoundationRepository repository;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public DocumentRenewalDeleteResponse hardDelete(String requestNoInput) {
        String requestNo = normalizeRequestNo(requestNoInput);
        UsersEntity user = currentUser();
        DocumentRenewalRequestEntity request =
                repository.lockOwnedRequestByNo(requestNo, user.getMobileUuid());
        String fromStatus = request.getStatusCode();

        repository.hardDeleteRequest(request.getId());

        return new DocumentRenewalDeleteResponse(requestNo, fromStatus, "DELETED",
                DocumentRenewalAction.HARD_DELETE.name());
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
