package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.DocumentRenewalPriceEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.DocumentRenewalRepository;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentRenewalServiceTest {
    private final DocumentRenewalRepository repository = mock(DocumentRenewalRepository.class);
    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private final DocumentRenewalService service = new DocumentRenewalService(
            repository, documentRepository, httpServletRequest);

    @Test
    void mapsCorrectionAndCancelledProgress() {
        DocumentRenewalStatusEntity correction = status("Pending Applicant Correction");
        correction.setDocumentStatusCode("PENDING_APPLICANT_CORRECTION");
        DocumentRenewalStatusEntity cancelled = status("Cancelled");
        when(repository.findActiveStatuses()).thenReturn(Arrays.asList(correction, cancelled));

        java.util.List<DocumentRenewalStatusResponse> result = service.statuses();

        assertEquals("PENDING_APPLICANT_CORRECTION", result.get(0).getDocumentStatusCode());
        assertEquals(1, result.get(0).getProgressStep());
        assertEquals("DOCUMENT_REVIEW",
                result.get(0).getMobileStatus().getDocumentMobileStatusCode());
        assertEquals("ตรวจเอกสาร", result.get(0).getMobileStatus().getNameTh());
        assertTrue(result.get(0).isCorrection());
        assertNull(result.get(1).getProgressStep());
        assertNull(result.get(1).getMobileStatus());
        assertTrue(result.get(1).isTerminal());
    }

    @Test
    void mapsPaymentPendingToDocumentReviewProgress() {
        DocumentRenewalStatusEntity paymentPending = status("Payment Pending");
        paymentPending.setDocumentStatusCode("PAYMENT_PENDING");
        when(repository.findActiveStatuses()).thenReturn(Collections.singletonList(paymentPending));

        java.util.List<DocumentRenewalStatusResponse> result = service.statuses();

        assertEquals("PAYMENT_PENDING", result.get(0).getDocumentStatusCode());
        assertEquals(1, result.get(0).getProgressStep());
        assertEquals("DOCUMENT_REVIEW",
                result.get(0).getMobileStatus().getDocumentMobileStatusCode());
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

    @Test
    void listsRenewalDocumentsWithLocalizedName() {
        DocumentEntity entity = new DocumentEntity();
        entity.setDocumentCode("DOC001");
        entity.setDocumentNameEn("Passport");
        entity.setDocumentNameTh("Passport TH");
        when(httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE)).thenReturn(AppSys.LANG_EN);
        when(documentRepository.findRenewalDocuments()).thenReturn(Collections.singletonList(entity));

        List<DocumentResponse> result = service.documents();

        assertEquals("DOC001", result.get(0).getDocumentCode());
        assertEquals("Passport", result.get(0).getDocumentName());
        assertEquals("Passport TH", result.get(0).getDocumentNameTh());
    }

    private DocumentRenewalStatusEntity status(String nameEn) {
        DocumentRenewalStatusEntity entity = new DocumentRenewalStatusEntity();
        entity.setNameEn(nameEn);
        if (!"Cancelled".equals(nameEn)) {
            entity.setDocumentMobileStatusCode("DOCUMENT_REVIEW");
            entity.setDocumentMobileStatusNameTh("ตรวจเอกสาร");
            entity.setDocumentMobileStatusNameEn("Document Review");
        }
        return entity;
    }
}
