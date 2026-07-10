package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentRenewalCreateService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");

    private final DocumentRenewalCreateRepository createRepository;
    private final DocumentRenewalFoundationRepository foundationRepository;
    private final DocumentRenewalService renewalService;
    private final DocumentRepository documentRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public DocumentRenewalCreateResponse create(DocumentRenewalCreateRequest input) {
        UsersEntity user = currentUser();
        String documentCode = input.getDocumentCode().trim().toUpperCase(Locale.ROOT);
        validateAddressId(input.getDeliveryAddressId());

        DocumentRenewalPriceResponse price = renewalService.price(documentCode);
        int requiredCount = createRepository.countActiveRequiredItems(documentCode);
        if (requiredCount == 0) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "requiredDocumentItems");
        }
        List<DocumentRequestItemEntity> requiredItems = documentRepository
                .findMissingItemsByUserAndDocumentCode(user.getMobileUuid(), documentCode);
        List<DocumentRequestItemEntity> missing = requiredItems == null ? null : requiredItems.stream()
                .filter(item -> !"COMPLETE".equalsIgnoreCase(item.getDocumentStatus()))
                .collect(Collectors.toList());
        if (missing != null && !missing.isEmpty()) {
            throw new BusinessException(AppStatus.MISSING_PARAMETER, "requiredDocumentItems", missing);
        }
        DeliveryAddressEntity address = deliveryAddressRepository
                .findActiveOwned(input.getDeliveryAddressId(), user.getMobileUuid());
        if (address == null) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "deliveryAddress");
        }

        String requestId = UUID.randomUUID().toString();
        String period = YearMonth.now(BUSINESS_ZONE).format(DateTimeFormatter.ofPattern("yyMM"));
        String requestNo = createRepository.nextRequestNo(period);
        String statusId = foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING);

        createRepository.insertRequest(requestId, requestNo, user.getMobileUuid(),
                documentCode, statusId, price.getPriceSettingId(), address.getId(), price.getTotal());
        int itemCount = createRepository.insertRequestItems(requestId, documentCode);
        if (itemCount != requiredCount) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "documentRenewalRequestItems");
        }
        foundationRepository.appendTransaction(requestId, DocumentRenewalAction.CREATE,
                null, DocumentRenewalStatus.PAYMENT_PENDING, "Unpaid draft created", user.getMobileUuid());

        DocumentRenewalCreateResponse response = new DocumentRenewalCreateResponse();
        response.setRequestId(requestId);
        response.setRequestNo(requestNo);
        response.setDocumentCode(documentCode);
        response.setStatus(DocumentRenewalStatus.PAYMENT_PENDING.name());
        response.setAmount(price.getTotal());
        response.setDeliveryAddressId(address.getId());
        return response;
    }

    private UsersEntity currentUser() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user;
    }

    private void validateAddressId(String addressId) {
        try {
            UUID.fromString(addressId);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_UUID, "deliveryAddressId");
        }
    }
}
