package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRenewalPriceEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.repository.DocumentRenewalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentRenewalService {
    private final DocumentRenewalRepository repository;

    public List<DocumentRenewalStatusResponse> statuses() {
        return repository.findActiveStatuses().stream().map(this::mapStatus).collect(Collectors.toList());
    }

    public DocumentRenewalPriceResponse price(String documentCode) {
        String normalizedCode = normalizeDocumentCode(documentCode);
        List<DocumentRenewalPriceEntity> prices = repository.findActivePrices(normalizedCode);
        if (prices.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentCode");
        }
        if (prices.size() != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Duplicate active renewal price for " + normalizedCode);
        }
        DocumentRenewalPriceEntity price = prices.get(0);
        DocumentRenewalPriceResponse response = new DocumentRenewalPriceResponse();
        response.setPriceSettingId(price.getId());
        response.setDocumentCode(price.getDocumentCode());
        response.setGovernmentFee(money(price.getGovernmentFee()));
        response.setDocumentProcessingFee(money(price.getDocumentProcessingFee()));
        response.setShippingFee(money(price.getShippingFee()));
        response.setShippingDiscount(money(price.getShippingDiscount()));
        response.setServiceFeeDiscount(money(price.getServiceFeeDiscount()));
        response.setEffectiveFrom(price.getEffectiveFrom());
        response.setEffectiveTo(price.getEffectiveTo());
        BigDecimal total = response.getGovernmentFee().add(response.getDocumentProcessingFee())
                .add(response.getShippingFee()).subtract(response.getShippingDiscount())
                .subtract(response.getServiceFeeDiscount());
        if (total.signum() < 0) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Renewal price discounts exceed fees for " + normalizedCode);
        }
        response.setTotal(total);
        return response;
    }

    private String normalizeDocumentCode(String documentCode) {
        if (documentCode == null || documentCode.trim().isEmpty()
                || documentCode.trim().length() > 10
                || !documentCode.trim().matches("[A-Za-z0-9_-]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentCode");
        }
        return documentCode.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private DocumentRenewalStatusResponse mapStatus(DocumentRenewalStatusEntity entity) {
        DocumentRenewalStatusResponse response = new DocumentRenewalStatusResponse();
        response.setId(entity.getId());
        response.setDocumentStatusCode(entity.getDocumentStatusCode());
        response.setNameTh(entity.getNameTh());
        response.setNameEn(entity.getNameEn());
        response.setCssColor(entity.getCssColor());
        response.setProgressStep(progressStep(entity.getNameEn()));
        response.setCorrection("Pending Applicant Correction".equals(entity.getNameEn()));
        response.setTerminal("Cancelled".equals(entity.getNameEn()) || "Delivered".equals(entity.getNameEn()));
        return response;
    }

    private Integer progressStep(String nameEn) {
        if ("Pending Document Review".equals(nameEn) || "Pending Applicant Correction".equals(nameEn)) return 1;
        if ("Pending Marine Department Result".equals(nameEn)) return 2;
        if ("Pending Department Document Pickup".equals(nameEn)) return 3;
        if ("Delivering".equals(nameEn)) return 4;
        if ("Delivered".equals(nameEn)) return 5;
        return null;
    }
}
