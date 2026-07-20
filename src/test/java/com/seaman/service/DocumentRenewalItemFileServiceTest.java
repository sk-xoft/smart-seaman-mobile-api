package com.seaman.service;

import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalItemFileServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock DocumentRequestItemFileService fileService;
    @Mock DocumentRenewalRequestItemFileService requestItemFileService;
    @Mock HttpServletRequest request;
    @Mock MultipartFile file;

    private DocumentRenewalItemFileService service;
    private String requestNo;
    private String documentRequestItemCode;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalItemFileService(
                repository, fileService, requestItemFileService, request);
        requestNo = "260700001";
        documentRequestItemCode = "MRI001";
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
    }

    @Test
    void replacesFixItemForOwnedCorrectionRequest() {
        RenewalRequestItemEntity item = correctionItem("FIX");
        when(repository.lockOwnedRequestItem(
                requestNo, documentRequestItemCode, "user-uuid")).thenReturn(item);
        DocumentRequestItemUploadResponse expected = new DocumentRequestItemUploadResponse();
        when(fileService.upload("MRI001", "ID_CARD", "FRONT", file)).thenReturn(expected);

        assertEquals(expected, service.upload(
                requestNo, documentRequestItemCode, "ID_CARD", "FRONT", file));
        verify(fileService).upload("MRI001", "ID_CARD", "FRONT", file);
    }

    @Test
    void storesRequestScopedCorrectionFileOnRenewalRequestItem() {
        RenewalRequestItemEntity item = correctionItem("FIX");
        item.setId("request-item-id");
        item.setStorageScope("REQUEST");
        when(repository.lockOwnedRequestItem(
                requestNo, documentRequestItemCode, "user-uuid")).thenReturn(item);
        DocumentRequestItemUploadResponse expected = new DocumentRequestItemUploadResponse();
        when(requestItemFileService.upload("request-item-id", "MRI001", "ID_CARD", "FRONT", file))
                .thenReturn(expected);

        assertEquals(expected, service.upload(
                requestNo, documentRequestItemCode, "ID_CARD", "FRONT", file));
        verify(requestItemFileService).upload(
                "request-item-id", "MRI001", "ID_CARD", "FRONT", file);
        verifyNoInteractions(fileService);
    }

    @Test
    void storesRequestScopedFileOnUnpaidDraftBeforePayment() {
        RenewalRequestItemEntity item = correctionItem("PENDING");
        item.setId("request-item-id");
        item.setStorageScope("REQUEST");
        item.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(repository.lockOwnedRequestItem(
                requestNo, documentRequestItemCode, "user-uuid")).thenReturn(item);
        DocumentRequestItemUploadResponse expected = new DocumentRequestItemUploadResponse();
        when(requestItemFileService.upload("request-item-id", "MRI001", "ID_CARD", "FRONT", file))
                .thenReturn(expected);

        assertEquals(expected, service.upload(
                requestNo, documentRequestItemCode, "ID_CARD", "FRONT", file));
        verify(requestItemFileService).upload(
                "request-item-id", "MRI001", "ID_CARD", "FRONT", file);
        verifyNoInteractions(fileService);
    }

    @Test
    void rejectsRequestOutsideCorrectionState() {
        RenewalRequestItemEntity item = correctionItem("FIX");
        item.setStatusNameEn(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.getMasterNameEn());
        when(repository.lockOwnedRequestItem(
                requestNo, documentRequestItemCode, "user-uuid")).thenReturn(item);

        assertThrows(BusinessException.class,
                () -> service.upload(requestNo, documentRequestItemCode, "GENERAL", "MAIN", file));
        verifyNoInteractions(fileService);
    }

    @Test
    void rejectsItemThatWasNotMarkedForCorrection() {
        when(repository.lockOwnedRequestItem(requestNo, documentRequestItemCode, "user-uuid"))
                .thenReturn(correctionItem("PASS"));

        assertThrows(BusinessException.class,
                () -> service.upload(requestNo, documentRequestItemCode, "GENERAL", "MAIN", file));
        verifyNoInteractions(fileService);
    }

    @Test
    void rejectsInvalidIdentifiersBeforeDatabaseAccess() {
        assertThrows(BusinessException.class,
                () -> service.upload("invalid request", documentRequestItemCode,
                        "GENERAL", "MAIN", file));
        verifyNoInteractions(repository, fileService);
    }

    private RenewalRequestItemEntity correctionItem(String approveStatus) {
        RenewalRequestItemEntity item = new RenewalRequestItemEntity();
        item.setId("request-item-id");
        item.setDocumentMasterRequestItemCode("MRI001");
        item.setStorageScope("PROFILE");
        item.setApproveStatus(approveStatus);
        item.setStatusNameEn(DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn());
        return item;
    }
}
