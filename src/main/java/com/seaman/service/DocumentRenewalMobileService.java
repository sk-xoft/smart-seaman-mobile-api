package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalMobileRequest;
import com.seaman.model.response.DocumentRenewalMobileResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class DocumentRenewalMobileService {
    private final DocumentRenewalFoundationRepository repository;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public DocumentRenewalMobileResponse update(String requestNo, DocumentRenewalMobileRequest input) {
        UsersEntity user = currentUser();
        String mobileNumber = input.getMobileNumber().trim();
        DocumentRenewalRequestEntity request =
                repository.lockOwnedRequestByNo(requestNo, user.getMobileUuid());

        repository.updateRequestMobileNumber(request.getId(), mobileNumber);
        repository.updateDeliveryAddressMobileNumber(request.getId(), user.getMobileUuid(), mobileNumber);

        return new DocumentRenewalMobileResponse(request.getRequestNo(), mobileNumber);
    }

    private UsersEntity currentUser() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user;
    }
}
