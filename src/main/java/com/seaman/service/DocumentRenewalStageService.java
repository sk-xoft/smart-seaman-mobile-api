package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalMobileStatusResponse;
import com.seaman.model.response.DocumentRenewalStageItemResponse;
import com.seaman.model.response.DocumentRenewalStageResponse;
import com.seaman.model.response.DocumentRenewalSummaryStatusResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentRenewalStageService {
    private static final int LADDER_SIZE = 5;

    private final DocumentRenewalFoundationRepository foundationRepository;
    private final DocumentRenewalRepository renewalRepository;
    private final HttpServletRequest httpServletRequest;

    public DocumentRenewalStageResponse stage(String requestNoInput) {
        String requestNo = normalizeRequestNo(requestNoInput);
        String userUuid = currentUserUuid();
        DocumentRenewalRequestEntity request =
                foundationRepository.findOwnedRequestByNo(requestNo, userUuid);

        DocumentRenewalSummaryStatusResponse currentStatus = status(request);
        Integer currentStep = currentStatus.getStep();

        DocumentRenewalStageResponse response = new DocumentRenewalStageResponse();
        response.setRequestNo(requestNo);
        response.setCurrentStatus(currentStatus);
        response.setStages(stages(currentStep));
        return response;
    }

    private List<DocumentRenewalStageItemResponse> stages(Integer currentStep) {
        Map<Integer, DocumentRenewalStatusEntity> ladder = new LinkedHashMap<>();
        for (DocumentRenewalStatusEntity row : renewalRepository.findActiveStatuses()) {
            Integer step = progressStep(row.getDocumentMobileStatusCode());
            if (step == null || ladder.containsKey(step)) continue;
            ladder.put(step, row);
        }
        List<DocumentRenewalStageItemResponse> result = new ArrayList<>();
        for (int step = 1; step <= LADDER_SIZE; step++) {
            DocumentRenewalStatusEntity row = ladder.get(step);
            if (row == null) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "documentRenewalStageLadder");
            }
            result.add(stageItem(row, step, currentStep));
        }
        return result;
    }

    private DocumentRenewalStageItemResponse stageItem(
            DocumentRenewalStatusEntity row, int step, Integer currentStep) {
        DocumentRenewalStageItemResponse item = new DocumentRenewalStageItemResponse();
        item.setStep(step);
        item.setDocumentMobileStatusCode(row.getDocumentMobileStatusCode());
        item.setNameTh(row.getDocumentMobileStatusNameTh());
        item.setNameEn(row.getDocumentMobileStatusNameEn());
        item.setState(state(step, currentStep));
        return item;
    }

    private String state(int step, Integer currentStep) {
        if (currentStep == null) return "PENDING";
        if (step < currentStep) return "DONE";
        if (step == currentStep) return "CURRENT";
        return "PENDING";
    }

    private DocumentRenewalSummaryStatusResponse status(DocumentRenewalRequestEntity request) {
        DocumentRenewalSummaryStatusResponse status = new DocumentRenewalSummaryStatusResponse();
        status.setId(request.getDocumentStatusId());
        status.setDocumentStatusCode(request.getStatusCode());
        status.setNameTh(request.getStatusNameTh());
        status.setNameEn(request.getStatusNameEn());
        status.setCssColor(request.getStatusCssColor());
        status.setMobileStatus(mobileStatus(
                request.getDocumentMobileStatusCode(),
                request.getDocumentMobileStatusNameTh(),
                request.getDocumentMobileStatusNameEn()));
        status.setStep(status.getMobileStatus() == null
                ? null : status.getMobileStatus().getStep());
        return status;
    }

    private DocumentRenewalMobileStatusResponse mobileStatus(
            String code, String nameTh, String nameEn) {
        Integer step = progressStep(code);
        if (step == null) return null;
        DocumentRenewalMobileStatusResponse response =
                new DocumentRenewalMobileStatusResponse();
        response.setDocumentMobileStatusCode(code);
        response.setNameTh(nameTh);
        response.setNameEn(nameEn);
        response.setStep(step);
        return response;
    }

    private Integer progressStep(String code) {
        if ("DOCUMENT_REVIEW".equals(code)) return 1;
        if ("MARINE_DEPARTMENT_RESULT".equals(code)) return 2;
        if ("DEPARTMENT_DOCUMENT_PICKUP".equals(code)) return 3;
        if ("DELIVERING".equals(code)) return 4;
        if ("DELIVERED".equals(code)) return 5;
        return null;
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
