package com.seaman.service;

import com.seaman.entity.DocumentRenewalPriceEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.repository.DocumentRenewalRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentRenewalServiceTest {
    private final DocumentRenewalRepository repository = mock(DocumentRenewalRepository.class);
    private final DocumentRenewalService service = new DocumentRenewalService(repository);

    @Test
    void mapsCorrectionAndCancelledProgress() {
        DocumentRenewalStatusEntity correction = status("Pending Applicant Correction");
        DocumentRenewalStatusEntity cancelled = status("Cancelled");
        when(repository.findActiveStatuses()).thenReturn(Arrays.asList(correction, cancelled));

        java.util.List<DocumentRenewalStatusResponse> result = service.statuses();

        assertEquals(1, result.get(0).getProgressStep());
        assertTrue(result.get(0).isCorrection());
        assertNull(result.get(1).getProgressStep());
        assertTrue(result.get(1).isTerminal());
    }

    @Test
    void calculatesPriceUsingBigDecimal() {
        DocumentRenewalPriceEntity entity = new DocumentRenewalPriceEntity();
        entity.setDocumentCode("DOC001");
        entity.setGovernmentFee(new BigDecimal("100.10"));
        entity.setDocumentProcessingFee(new BigDecimal("20.20"));
        entity.setShippingFee(new BigDecimal("30.30"));
        entity.setShippingDiscount(new BigDecimal("5.05"));
        entity.setServiceFeeDiscount(new BigDecimal("10.10"));
        when(repository.findActivePrices("DOC001")).thenReturn(Collections.singletonList(entity));

        DocumentRenewalPriceResponse result = service.price(" doc001 ");

        assertEquals(new BigDecimal("135.45"), result.getTotal());
        verify(repository).findActivePrices("DOC001");
    }

    @Test
    void rejectsMissingPrice() {
        when(repository.findActivePrices("DOC001")).thenReturn(Collections.emptyList());
        assertThrows(BusinessException.class, () -> service.price("DOC001"));
    }

    @Test
    void rejectsDuplicatePriceConfiguration() {
        when(repository.findActivePrices("DOC001")).thenReturn(Arrays.asList(
                new DocumentRenewalPriceEntity(), new DocumentRenewalPriceEntity()));
        assertThrows(BusinessException.class, () -> service.price("DOC001"));
    }

    @Test
    void rejectsPriceWhoseDiscountsExceedFees() {
        DocumentRenewalPriceEntity entity = new DocumentRenewalPriceEntity();
        entity.setDocumentCode("DOC001");
        entity.setGovernmentFee(BigDecimal.ONE);
        entity.setDocumentProcessingFee(BigDecimal.ZERO);
        entity.setShippingFee(BigDecimal.ZERO);
        entity.setShippingDiscount(BigDecimal.TEN);
        entity.setServiceFeeDiscount(BigDecimal.ZERO);
        when(repository.findActivePrices("DOC001")).thenReturn(Collections.singletonList(entity));
        assertThrows(BusinessException.class, () -> service.price("DOC001"));
    }

    @Test
    void validatesDocumentCode() {
        assertThrows(BusinessException.class, () -> service.price(""));
        assertThrows(BusinessException.class, () -> service.price("DOC 001"));
        verifyNoInteractions(repository);
    }

    private DocumentRenewalStatusEntity status(String nameEn) {
        DocumentRenewalStatusEntity entity = new DocumentRenewalStatusEntity();
        entity.setNameEn(nameEn);
        return entity;
    }
}
