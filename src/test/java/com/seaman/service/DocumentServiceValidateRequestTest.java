package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.request.DocumentRequestValidateRequest;
import com.seaman.model.response.DocumentRequestValidateResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import com.seaman.utils.Base64FileValidator;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceValidateRequestTest {
    @Mock HttpServletRequest httpServletRequest;
    @Mock DocumentRepository documentRepository;
    @Mock DocumentRenewalCreateRepository createRepository;
    @Mock DocumentRenewalFoundationRepository foundationRepository;
    @Mock DocumentRenewalService renewalService;
    @Mock DocumentRequestItemFileRepository itemFileRepository;
    @Mock DocumentRequestItemFileService itemFileService;
    @Mock CertificateRepository certificateRepository;
    @Mock DateUtil dateUtil;
    @Mock AmazonS3 s3;
    @Mock FrameworkUtils frameworkUtils;
    @Mock Base64FileValidator base64FileValidator;
    @Mock TransactionLogsService transactionLogsService;

    private DocumentService service;
    private DocumentRequestValidateRequest request;

    @BeforeEach
    void setUp() {
        service = new DocumentService(httpServletRequest, documentRepository, createRepository,
                foundationRepository, renewalService, itemFileRepository, itemFileService,
                certificateRepository, dateUtil, s3, frameworkUtils, base64FileValidator,
                transactionLogsService);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        user.setUsername("user@example.com");
        when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        request = new DocumentRequestValidateRequest();
        request.setDocumentCode("doc001");
    }

    @Test
    void createsPaymentPendingRequestAndReturnsMissingItemsWhenProfileDocumentsAreIncomplete() {
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(item("PROFILE", "MISSING")));
        stubRequestCreation(1);

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("MISSING", response.getItems().get(0).getDocumentStatus());
        assertEquals("request-id", response.getRequestId());
        assertEquals("260700001", response.getRequestNo());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "DOC001", "payment-status-id", "price-setting-id",
                null, new BigDecimal("1500.00"));
        verify(createRepository).insertRequestItems("request-id", "DOC001");
        verify(foundationRepository).appendTransaction("request-id", DocumentRenewalAction.CREATE,
                null, DocumentRenewalStatus.PAYMENT_PENDING, "Unpaid draft created",
                "mobile-user-uuid");
    }

    @Test
    void createsPaymentPendingRequestWhenProfileDocumentsAreComplete() {
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Arrays.asList(item("PROFILE", "COMPLETE"), item("REQUEST", "MISSING")));
        stubRequestCreation(2);

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("request-id", response.getRequestId());
        assertEquals("260700001", response.getRequestNo());
        assertEquals(2, response.getItems().size());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "DOC001", "payment-status-id", "price-setting-id",
                null, new BigDecimal("1500.00"));
        verify(createRepository).insertRequestItems("request-id", "DOC001");
        verify(foundationRepository).appendTransaction("request-id", DocumentRenewalAction.CREATE,
                null, DocumentRenewalStatus.PAYMENT_PENDING, "Unpaid draft created",
                "mobile-user-uuid");
    }

    @Test
    void returnsExistingNonDeliveredRequestItemsInsteadOfCreatingAgain() {
        DocumentRenewalRequestEntity existing = new DocumentRenewalRequestEntity();
        existing.setId("existing-request-id");
        existing.setRequestNo("260700099");
        existing.setStatusCode("PENDING_DOCUMENT_REVIEW");
        when(createRepository.findLatestActiveRequestNotDelivered("mobile-user-uuid", "DOC001"))
                .thenReturn(existing);
        when(createRepository.findRequestItemsForValidate("existing-request-id", "mobile-user-uuid"))
                .thenReturn(Collections.singletonList(item("REQUEST", "MISSING")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("existing-request-id", response.getRequestId());
        assertEquals("260700099", response.getRequestNo());
        assertEquals("MISSING", response.getItems().get(0).getDocumentStatus());
        verify(documentRepository, never()).findMissingItemsByUserAndDocumentCode(anyString(), anyString());
        verify(renewalService, never()).price(anyString());
        verify(createRepository, never()).insertRequest(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any());
        verify(createRepository, never()).insertRequestItems(anyString(), anyString());
        verify(foundationRepository, never()).appendTransaction(anyString(), any(),
                any(), any(), anyString(), anyString());
    }

    private void stubRequestCreation(int itemCount) {
        DocumentRenewalPriceResponse price = new DocumentRenewalPriceResponse();
        price.setPriceSettingId("price-setting-id");
        price.setTotal(new BigDecimal("1500.00"));
        when(renewalService.price("DOC001")).thenReturn(price);
        when(frameworkUtils.generateUUID()).thenReturn("request-id");
        when(createRepository.nextRequestNo(anyString())).thenReturn("260700001");
        when(foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING))
                .thenReturn("payment-status-id");
        when(createRepository.insertRequestItems("request-id", "DOC001")).thenReturn(itemCount);
    }

    private DocumentRequestItemEntity item(String storageScope, String status) {
        DocumentRequestItemEntity item = new DocumentRequestItemEntity();
        item.setDocumentCode("DOC001");
        item.setMobileUserUuid("mobile-user-uuid");
        item.setDocumentMasterRequestItemCode("MRI002");
        item.setStorageScope(storageScope);
        item.setDocumentStatus(status);
        return item;
    }
}
