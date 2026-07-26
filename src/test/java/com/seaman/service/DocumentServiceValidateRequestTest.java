package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.request.DocumentRequestValidateRequest;
import com.seaman.model.response.DocumentRequestValidateResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalRequestItemFileRepository;
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
import java.util.List;

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
    @Mock DocumentRenewalRequestItemFileRepository renewalRequestItemFileRepository;
    @Mock DocumentRenewalRequestItemFileService renewalRequestItemFileService;
    @Mock CertificateRepository certificateRepository;
    @Mock DeliveryAddressRepository deliveryAddressRepository;
    @Mock DateUtil dateUtil;
    @Mock AmazonS3 s3;
    @Mock FrameworkUtils frameworkUtils;
    @Mock Base64FileValidator base64FileValidator;
    @Mock TransactionLogsService transactionLogsService;

    private DocumentService service;
    private DocumentRequestValidateRequest request;
    private DeliveryAddressEntity defaultAddress;

    @BeforeEach
    void setUp() {
        service = new DocumentService(httpServletRequest, documentRepository, createRepository,
                foundationRepository, renewalService, itemFileRepository, itemFileService,
                renewalRequestItemFileRepository, renewalRequestItemFileService, certificateRepository,
                deliveryAddressRepository, dateUtil, s3, frameworkUtils, base64FileValidator,
                transactionLogsService);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        user.setMobileNumber("0812345678");
        user.setEmail("crew@example.com");
        user.setUsername("user@example.com");
        when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        DocumentEntity document = new DocumentEntity();
        document.setDocumentCode("DOC001");
        document.setDocumentNameTh("ประกาศนียบัตรลูกเรือ");
        document.setDocumentNameEn("Seafarer Certificate");
        when(documentRepository.findByDocumentCode("DOC001")).thenReturn(document);
        CertificateEntity certificate = new CertificateEntity();
        certificate.setCertEndDate("2027-12-31 00:00:00");
        lenient().when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(certificate));
        lenient().when(renewalRequestItemFileRepository.findFiles(anyString()))
                .thenReturn(Collections.<DocumentRequestItemFileEntity>emptyList());
        lenient().when(renewalRequestItemFileService.mapFiles(anyList()))
                .thenReturn(Collections.emptyList());
        defaultAddress = new DeliveryAddressEntity();
        defaultAddress.setId("default-address-id");
        defaultAddress.setMobileUserUuid("mobile-user-uuid");
        defaultAddress.setFirstName("Somchai");
        defaultAddress.setLastName("Seaman");
        defaultAddress.setAddressLine("1 Ocean Road");
        defaultAddress.setProvince("Bangkok");
        defaultAddress.setDistrict("Bang Rak");
        defaultAddress.setSubDistrict("Si Lom");
        defaultAddress.setPostalCode("10500");
        defaultAddress.setDescription("1 Ocean Road ตำบลSi Lom อำเภอBang Rak จังหวัดBangkok 10500");
        request = new DocumentRequestValidateRequest();
        request.setDocumentCode("doc001");
    }

    @Test
    void createsPaymentPendingRequestAndReturnsProfileItemStatusFromValidation() {
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(item("PROFILE", "MISSING")));
        stubRequestCreation(1, Collections.singletonList(item("PROFILE", "MISSING")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("ประกาศนียบัตรลูกเรือ", response.getDocumentName());
        assertEquals("ประกาศนียบัตรลูกเรือ", response.getDocumentNameTh());
        assertEquals("Seafarer Certificate", response.getDocumentNameEn());
        assertEquals("2027-12-31", response.getCertEndDate());
        assertEquals("MISSING", response.getItems().get(0).getDocumentStatus());
        assertEquals("request-id", response.getRequestId());
        assertEquals("260700001", response.getRequestNo());
        assertEquals(null, response.getIdempotencyKey());
        assertEquals("0812345678", response.getMobileNumber());
        assertEquals("crew@example.com", response.getEmail());
        assertEquals(1, response.getAddress().size());
        assertEquals("default-address-id", response.getAddress().get(0).getId());
        assertEquals("Somchai", response.getAddress().get(0).getFirstName());
        assertEquals("0812345678", response.getAddress().get(0).getMobileNumber());
        assertEquals("1 Ocean Road ตำบลSi Lom อำเภอBang Rak จังหวัดBangkok 10500",
                response.getAddress().get(0).getDescription());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "0812345678", "crew@example.com", "DOC001",
                "payment-status-id", "price-setting-id", "default-address-id",
                new BigDecimal("1500.00"), null);
        verify(createRepository).insertDeliveryAddressSnapshot(
                "request-id", defaultAddress, "0812345678");
        verify(createRepository).insertRequestItems("request-id", "DOC001");
        verify(foundationRepository).appendTransaction("request-id", DocumentRenewalAction.CREATE,
                null, DocumentRenewalStatus.PAYMENT_PENDING, "Unpaid draft created",
                "mobile-user-uuid");
    }

    @Test
    void createsPaymentPendingRequestWithIdempotencyKeyInResponse() {
        request.setIdempotencyKey("renewal-doc001-attempt-1");
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(item("PROFILE", "MISSING")));
        stubRequestCreation(1, Collections.singletonList(item("PROFILE", "MISSING")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("request-id", response.getRequestId());
        assertEquals("260700001", response.getRequestNo());
        assertEquals("renewal-doc001-attempt-1", response.getIdempotencyKey());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "0812345678", "crew@example.com", "DOC001",
                "payment-status-id", "price-setting-id", "default-address-id",
                new BigDecimal("1500.00"), "renewal-doc001-attempt-1");
    }

    @Test
    void createsPaymentPendingRequestWhenProfileDocumentsAreComplete() {
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Arrays.asList(item("MRI002", "PROFILE", "COMPLETE"),
                        item("MRI004", "REQUEST", "MISSING")));
        stubRequestCreation(2, Arrays.asList(item("MRI002", "PROFILE", "COMPLETE"),
                item("MRI004", "REQUEST", "MISSING")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("request-id", response.getRequestId());
        assertEquals("260700001", response.getRequestNo());
        assertEquals(null, response.getIdempotencyKey());
        assertEquals("ประกาศนียบัตรลูกเรือ", response.getDocumentName());
        assertEquals("0812345678", response.getMobileNumber());
        assertEquals("crew@example.com", response.getEmail());
        assertEquals(1, response.getAddress().size());
        assertEquals("default-address-id", response.getAddress().get(0).getId());
        assertEquals("1 Ocean Road ตำบลSi Lom อำเภอBang Rak จังหวัดBangkok 10500",
                response.getAddress().get(0).getDescription());
        assertEquals(2, response.getItems().size());
        assertEquals("MRI002", response.getItems().get(0).getDocumentMasterRequestItemCode());
        assertEquals("COMPLETE", response.getItems().get(0).getDocumentStatus());
        assertEquals("MRI004", response.getItems().get(1).getDocumentMasterRequestItemCode());
        assertEquals("MISSING", response.getItems().get(1).getDocumentStatus());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "0812345678", "crew@example.com", "DOC001",
                "payment-status-id", "price-setting-id", "default-address-id",
                new BigDecimal("1500.00"), null);
        verify(createRepository).insertDeliveryAddressSnapshot(
                "request-id", defaultAddress, "0812345678");
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
        existing.setIdempotencyKey("existing-key");
        existing.setMobileNumber("0899999999");
        existing.setEmail("snapshot@example.com");
        when(createRepository.findLatestActiveRequestNotDelivered("mobile-user-uuid", "DOC001"))
                .thenReturn(existing);
        when(createRepository.findRequestItemsForValidate("existing-request-id", "mobile-user-uuid"))
                .thenReturn(Collections.singletonList(item("REQUEST", "MISSING")));
        DeliveryAddressEntity snapshotAddress = deliveryAddress("snapshot-address-id");
        snapshotAddress.setMobileNumber("0899999999");
        snapshotAddress.setFirstName("Snapshot");
        snapshotAddress.setDescription("2 Snapshot Road ตำบลSi Lom อำเภอBang Rak จังหวัดBangkok 10500");
        when(createRepository.findDeliveryAddressSnapshot("existing-request-id", "mobile-user-uuid"))
                .thenReturn(Collections.singletonList(snapshotAddress));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("ประกาศนียบัตรลูกเรือ", response.getDocumentName());
        assertEquals("ประกาศนียบัตรลูกเรือ", response.getDocumentNameTh());
        assertEquals("Seafarer Certificate", response.getDocumentNameEn());
        assertEquals("2027-12-31", response.getCertEndDate());
        assertEquals("existing-request-id", response.getRequestId());
        assertEquals("260700099", response.getRequestNo());
        assertEquals("existing-key", response.getIdempotencyKey());
        assertEquals("0899999999", response.getMobileNumber());
        assertEquals("snapshot@example.com", response.getEmail());
        assertEquals(1, response.getAddress().size());
        assertEquals("snapshot-address-id", response.getAddress().get(0).getId());
        assertEquals("Snapshot", response.getAddress().get(0).getFirstName());
        assertEquals("0899999999", response.getAddress().get(0).getMobileNumber());
        assertEquals("2 Snapshot Road ตำบลSi Lom อำเภอBang Rak จังหวัดBangkok 10500",
                response.getAddress().get(0).getDescription());
        assertEquals("MISSING", response.getItems().get(0).getDocumentStatus());
        verify(documentRepository, never()).findMissingItemsByUserAndDocumentCode(anyString(), anyString());
        verify(renewalService, never()).price(anyString());
        verify(createRepository, never()).insertRequest(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verify(createRepository, never()).insertDeliveryAddressSnapshot(anyString(), any(), anyString());
        verify(createRepository, never()).insertRequestItems(anyString(), anyString());
        verify(foundationRepository, never()).appendTransaction(anyString(), any(),
                any(), any(), anyString(), anyString());
    }

    @Test
    void returnsExistingRequestForSameIdempotencyKeyInsteadOfCreatingAgain() {
        request.setIdempotencyKey("renewal-doc001-attempt-1");
        DocumentRenewalRequestEntity existing = new DocumentRenewalRequestEntity();
        existing.setId("idempotent-request-id");
        existing.setRequestNo("260700088");
        existing.setDocumentCode("DOC001");
        existing.setIdempotencyKey("renewal-doc001-attempt-1");
        existing.setMobileNumber("0877777777");
        existing.setEmail("idem@example.com");
        when(createRepository.findByIdempotencyKey("mobile-user-uuid", "renewal-doc001-attempt-1"))
                .thenReturn(existing);
        when(createRepository.findRequestItemsForValidate("idempotent-request-id", "mobile-user-uuid"))
                .thenReturn(Collections.singletonList(item("PROFILE", "COMPLETE")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("idempotent-request-id", response.getRequestId());
        assertEquals("260700088", response.getRequestNo());
        assertEquals("renewal-doc001-attempt-1", response.getIdempotencyKey());
        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("0877777777", response.getMobileNumber());
        assertEquals("idem@example.com", response.getEmail());
        assertEquals("COMPLETE", response.getItems().get(0).getDocumentStatus());
        verify(createRepository, never()).findLatestActiveRequestNotDelivered(anyString(), anyString());
        verify(documentRepository, never()).findMissingItemsByUserAndDocumentCode(anyString(), anyString());
        verify(renewalService, never()).price(anyString());
        verify(createRepository, never()).insertRequest(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void createsPaymentPendingRequestWithoutSnapshotWhenDefaultDeliveryAddressDoesNotExist() {
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(item("PROFILE", "MISSING")));
        DocumentRenewalPriceResponse price = new DocumentRenewalPriceResponse();
        price.setPriceSettingId("price-setting-id");
        price.setTotal(new BigDecimal("1500.00"));
        when(renewalService.price("DOC001")).thenReturn(price);
        when(deliveryAddressRepository.findActiveDefaults("mobile-user-uuid"))
                .thenReturn(Collections.<DeliveryAddressEntity>emptyList());
        when(frameworkUtils.generateUUID()).thenReturn("request-id");
        when(createRepository.nextRequestNo(anyString())).thenReturn("260700001");
        when(foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING))
                .thenReturn("payment-status-id");
        when(createRepository.insertRequestItems("request-id", "DOC001")).thenReturn(1);
        when(createRepository.findRequestItemsForValidate("request-id", "mobile-user-uuid"))
                .thenReturn(Collections.singletonList(item("PROFILE", "MISSING")));

        DocumentRequestValidateResponse response = service.validateAndCreateDocumentRenewalsItems(request);

        assertEquals("request-id", response.getRequestId());
        assertEquals(null, response.getIdempotencyKey());
        assertEquals("0812345678", response.getMobileNumber());
        assertEquals("crew@example.com", response.getEmail());
        assertEquals(0, response.getAddress().size());
        verify(createRepository).insertRequest("request-id", "260700001",
                "mobile-user-uuid", "0812345678", "crew@example.com", "DOC001",
                "payment-status-id", "price-setting-id", null, new BigDecimal("1500.00"), null);
        verify(createRepository, never()).insertDeliveryAddressSnapshot(anyString(), any(), anyString());
        verify(createRepository).insertRequestItems("request-id", "DOC001");
    }

    private void stubRequestCreation(int itemCount, List<DocumentRequestItemEntity> createdItems) {
        DocumentRenewalPriceResponse price = new DocumentRenewalPriceResponse();
        price.setPriceSettingId("price-setting-id");
        price.setTotal(new BigDecimal("1500.00"));
        when(renewalService.price("DOC001")).thenReturn(price);
        when(frameworkUtils.generateUUID()).thenReturn("request-id");
        when(createRepository.nextRequestNo(anyString())).thenReturn("260700001");
        when(foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING))
                .thenReturn("payment-status-id");
        when(deliveryAddressRepository.findActiveDefaults("mobile-user-uuid"))
                .thenReturn(Collections.singletonList(defaultAddress));
        when(createRepository.insertRequestItems("request-id", "DOC001")).thenReturn(itemCount);
        when(createRepository.findRequestItemsForValidate("request-id", "mobile-user-uuid"))
                .thenReturn(createdItems);
    }

    private DocumentRequestItemEntity item(String storageScope, String status) {
        return item("MRI002", storageScope, status);
    }

    private DocumentRequestItemEntity item(String itemCode, String storageScope, String status) {
        DocumentRequestItemEntity item = new DocumentRequestItemEntity();
        item.setId(itemCode + "-request-item-id");
        item.setDocumentCode("DOC001");
        item.setMobileUserUuid("mobile-user-uuid");
        item.setDocumentMasterRequestItemCode(itemCode);
        item.setStorageScope(storageScope);
        item.setDocumentStatus(status);
        return item;
    }

    private DeliveryAddressEntity deliveryAddress(String id) {
        DeliveryAddressEntity address = new DeliveryAddressEntity();
        address.setId(id);
        address.setMobileUserUuid("mobile-user-uuid");
        address.setFirstName("Somchai");
        address.setLastName("Seaman");
        address.setAddressLine("1 Ocean Road");
        address.setProvince("Bangkok");
        address.setDistrict("Bang Rak");
        address.setSubDistrict("Si Lom");
        address.setPostalCode("10500");
        return address;
    }
}
