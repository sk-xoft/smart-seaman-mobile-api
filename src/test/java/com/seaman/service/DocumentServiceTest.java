package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentUpdateResponse;
import com.seaman.model.response.PageDocumentResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

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
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new DocumentService(httpServletRequest, documentRepository, createRepository,
                foundationRepository, renewalService, itemFileRepository, itemFileService,
                renewalRequestItemFileRepository, renewalRequestItemFileService, certificateRepository,
                deliveryAddressRepository, dateUtil, s3, frameworkUtils, base64FileValidator,
                transactionLogsService);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "storePathTemplate", "documents/%s/certs/%s");

        user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        user.setMobileNumber("0812345678");
        user.setEmail("crew@example.com");
        user.setUsername("user@example.com");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
    }

    // ---------------------------------------------------------------- pageDocument

    @Test
    void pageDocumentReturnsFormattedItemsAndTotals() {
        DocumentEntity entity = documentEntity();
        when(documentRepository.findByPage("mobile-user-uuid", 0, "COT"))
                .thenReturn(Collections.singletonList(entity));
        when(documentRepository.countByPageByUserUid("mobile-user-uuid", "COT")).thenReturn(1);
        when(dateUtil.calculateDisplayDateCertRemain("2027-12-31"))
                .thenReturn(Period.of(1, 2, 3));
        when(dateUtil.formatDateToStr("2026-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2026-01-01");
        when(dateUtil.formatDateToStr("2027-12-31", DateUtil.YEAR_MONTH_DATE)).thenReturn("2027-12-31");

        PageDocumentResponse response = service.pageDocument(0, "COT");

        assertEquals(1, response.getItemTotal());
        assertEquals(true, response.isLast());
        assertEquals("1", response.getItems().get(0).getDisYear());
        assertEquals("2", response.getItems().get(0).getDisMonth());
        assertEquals("3", response.getItems().get(0).getDisDay());
        verify(transactionLogsService).insert(any(), any(), eq("CERTIFICATE OF TRAINING"), eq("user@example.com"));
        verify(transactionLogsService).update(any(), eq("{}"), eq(AppStatus.SUCCESS_CODE), eq("user@example.com"));
    }

    @Test
    void pageDocumentBlanksRemainingWhenCertDatesAreNull() {
        DocumentEntity entity = new DocumentEntity();
        entity.setDocumentCode("DOC001");
        when(documentRepository.findByPage("mobile-user-uuid", 0, "Document"))
                .thenReturn(Collections.singletonList(entity));
        when(documentRepository.countByPageByUserUid("mobile-user-uuid", "Document")).thenReturn(1);

        PageDocumentResponse response = service.pageDocument(0, "Document");

        assertEquals("", response.getItems().get(0).getDisYear());
        assertEquals("", response.getItems().get(0).getDisMonth());
        assertEquals("", response.getItems().get(0).getDisDay());
    }

    @Test
    void pageDocumentWrapsRepositoryFailureAsBusinessException() {
        when(documentRepository.findByPage(anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(BusinessException.class, () -> service.pageDocument(0, "COT"));
        verify(transactionLogsService).update(any(), eq("{}"), eq(AppStatus.EXCEPTION_GLOBAL), eq("user@example.com"));
    }

    // ------------------------------------------------------------ closeToExpiration

    @Test
    void closeToExpirationFlagsExpiredCertificates() {
        DocumentEntity entity = documentEntity();
        entity.setCertEndDate("2020-01-01");
        when(documentRepository.findCloseToExpiration("mobile-user-uuid", 0))
                .thenReturn(Collections.singletonList(entity));
        when(documentRepository.countByPageByUserUidCloseToExpiration("mobile-user-uuid")).thenReturn(1);
        when(dateUtil.calculateDisplayDateCertRemain("2020-01-01")).thenReturn(Period.of(0, 0, 0));
        when(dateUtil.formatDateToStr("2026-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2026-01-01");
        when(dateUtil.formatDateToStr("2020-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2020-01-01");

        PageDocumentResponse response = service.closeToExpiration(0);

        assertEquals("Y", response.getItems().get(0).getCertExpiredFlag());
    }

    @Test
    void closeToExpirationMarksFutureCertificatesAsNotExpired() {
        DocumentEntity entity = documentEntity();
        when(documentRepository.findCloseToExpiration("mobile-user-uuid", 0))
                .thenReturn(Collections.singletonList(entity));
        when(documentRepository.countByPageByUserUidCloseToExpiration("mobile-user-uuid")).thenReturn(1);
        when(dateUtil.calculateDisplayDateCertRemain("2027-12-31")).thenReturn(Period.of(1, 0, 0));
        when(dateUtil.formatDateToStr("2026-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2026-01-01");
        when(dateUtil.formatDateToStr("2027-12-31", DateUtil.YEAR_MONTH_DATE)).thenReturn("2027-12-31");

        PageDocumentResponse response = service.closeToExpiration(0);

        assertEquals("N", response.getItems().get(0).getCertExpiredFlag());
    }

    @Test
    void closeToExpirationWrapsRepositoryFailureAsBusinessException() {
        when(documentRepository.findCloseToExpiration(anyString(), anyInt()))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(BusinessException.class, () -> service.closeToExpiration(0));
    }

    // ----------------------------------------------------------------- documentCreate

    @Test
    void documentCreateInsertsCertificateAndUploadsFile() {
        DocumentCreateRequest request = createRequest();
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.emptyList());
        when(frameworkUtils.generateUUID()).thenReturn("new-file-name");
        when(certificateRepository.insert(any(CertificateEntity.class))).thenReturn(true);

        DocumentCreateResponse response = service.documentCreate(request);

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("2026-01-01", response.getCertStartDate());
        assertEquals("2027-12-31", response.getCertEndDate());
        verify(base64FileValidator).validateDocument("base64-file", "fileCert");
        verify(s3).putObject("smart-seaman-bucket", "documents/mobile-user-uuid/certs/new-file-name", "base64-file");
    }

    @Test
    void documentCreateRejectsWhenCertificateAlreadyExists() {
        DocumentCreateRequest request = createRequest();
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(new CertificateEntity()));

        assertThrows(BusinessException.class, () -> service.documentCreate(request));
        verify(certificateRepository, never()).insert(any());
        verify(s3, never()).putObject(anyString(), anyString(), anyString());
    }

    // ----------------------------------------------------------------- documentUpdate

    @Test
    void documentUpdateWithoutFileChangeSkipsUpload() {
        DocumentUpdateRequest request = updateRequest("N");
        CertificateEntity existing = new CertificateEntity();
        existing.setCertId("cert-id-1");
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(existing));
        when(certificateRepository.updateNoChangeFile(any(CertificateEntity.class))).thenReturn(true);

        DocumentUpdateResponse response = service.documentUpdate(request);

        assertEquals("DOC001", response.getDocumentCode());
        verify(certificateRepository).updateNoChangeFile(any(CertificateEntity.class));
        verify(certificateRepository, never()).update(any());
        verify(s3, never()).putObject(anyString(), anyString(), anyString());
    }

    @Test
    void documentUpdateWithFileChangeValidatesAndUploads() {
        DocumentUpdateRequest request = updateRequest("Y");
        CertificateEntity existing = new CertificateEntity();
        existing.setCertId("cert-id-1");
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(existing));
        when(frameworkUtils.generateUUID()).thenReturn("new-file-name");
        when(certificateRepository.update(any(CertificateEntity.class))).thenReturn(true);

        DocumentUpdateResponse response = service.documentUpdate(request);

        assertEquals("DOC001", response.getDocumentCode());
        verify(base64FileValidator).validateDocument("base64-file", "fileCert");
        verify(certificateRepository).update(any(CertificateEntity.class));
        verify(s3).putObject("smart-seaman-bucket", "documents/mobile-user-uuid/certs/new-file-name", "base64-file");
    }

    @Test
    void documentUpdateRejectsWhenCertificateDoesNotExist() {
        DocumentUpdateRequest request = updateRequest("N");
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> service.documentUpdate(request));
    }

    // ----------------------------------------------------------------- documentDelete

    @Test
    void documentDeleteRemovesExistingCertificate() {
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "CERT001"))
                .thenReturn(Collections.singletonList(new CertificateEntity()));
        when(certificateRepository.documentDelete("mobile-user-uuid", "CERT001")).thenReturn(true);

        String result = service.documentDelete("CERT001");

        assertEquals("success", result);
        verify(certificateRepository).documentDelete("mobile-user-uuid", "CERT001");
    }

    @Test
    void documentDeleteRejectsWhenCertificateDoesNotExist() {
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "CERT001"))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> service.documentDelete("CERT001"));
        verify(certificateRepository, never()).documentDelete(anyString(), anyString());
    }

    // ------------------------------------------------------------------- documentEdit

    @Test
    void documentEditReturnsActiveEndDateWhenPresent() {
        CertificateEntity existing = new CertificateEntity();
        existing.setCertDocumentCode("DOC001");
        existing.setCertStartDate("2026-01-01");
        existing.setCertEndDate("2027-12-31");
        existing.setOriginalFileName("cert.png");
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "CERT001"))
                .thenReturn(Collections.singletonList(existing));
        when(dateUtil.formatDateToStr("2026-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2026-01-01");
        when(dateUtil.formatDateToStr("2027-12-31", DateUtil.YEAR_MONTH_DATE)).thenReturn("2027-12-31");

        DocumentCreateResponse response = service.documentEdit("CERT001");

        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("A", response.getCertEndDateType());
        assertEquals("2027-12-31", response.getCertEndDate());
        assertEquals("cert.png", response.getFileCertName());
    }

    @Test
    void documentEditReturnsNoExpiryWhenEndDateIsNull() {
        CertificateEntity existing = new CertificateEntity();
        existing.setCertDocumentCode("DOC001");
        existing.setCertStartDate("2026-01-01");
        existing.setCertEndDate(null);
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "CERT001"))
                .thenReturn(Collections.singletonList(existing));
        when(dateUtil.formatDateToStr("2026-01-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("2026-01-01");

        DocumentCreateResponse response = service.documentEdit("CERT001");

        assertEquals("N", response.getCertEndDateType());
        assertEquals("9999-99-99", response.getCertEndDate());
    }

    @Test
    void documentEditRejectsWhenCertificateDoesNotExist() {
        when(certificateRepository.findByUsersAndCertCodeList("mobile-user-uuid", "CERT001"))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> service.documentEdit("CERT001"));
    }

    // --------------------------------------------------------------------- viewCert

    @Test
    void viewCertReturnsBase64FileFromObjectStorage() {
        CertificateEntity certificate = new CertificateEntity();
        certificate.setCertFile("stored-file");
        when(certificateRepository.findBy("mobile-user-uuid", "CERT001")).thenReturn(certificate);
        when(s3.getObjectAsString("smart-seaman-bucket", "documents/mobile-user-uuid/certs/stored-file"))
                .thenReturn("base64-content");

        String result = service.viewCert("CERT001");

        assertEquals("base64-content", result);
    }

    @Test
    void viewCertRejectsWhenCertificateNotFound() {
        when(certificateRepository.findBy("mobile-user-uuid", "CERT001")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.viewCert("CERT001"));
        assertEquals(AppStatus.DATA_NOT_FOUND, exception.getCode());
    }

    // ---------------------------------------------------------------- listProfileItems

    @Test
    void listProfileItemsAttachesFilesForUploadedProfileItemsOnly() {
        com.seaman.entity.DocumentRequestItemEntity missing =
                profileItem("MRI002", "MISSING", 0, null);
        com.seaman.entity.DocumentRequestItemEntity complete =
                profileItem("MRI001", "COMPLETE", 1, "profile-item-id");
        when(itemFileRepository.findProfileItems("mobile-user-uuid"))
                .thenReturn(List.of(missing, complete));
        com.seaman.entity.DocumentRequestItemFileEntity file =
                new com.seaman.entity.DocumentRequestItemFileEntity();
        file.setDocumentMasterRequestItemCode("MRI001");
        when(itemFileRepository.findFilesByItemCodes("mobile-user-uuid", List.of("MRI001")))
                .thenReturn(Collections.singletonList(file));
        when(itemFileService.mapFiles(Collections.singletonList(file)))
                .thenReturn(Collections.singletonList(new com.seaman.model.response.DocumentRequestItemFileResponse()));

        List<com.seaman.model.response.DocumentRequestItemResponse> response = service.listProfileItems();

        assertEquals(2, response.size());
        assertEquals("MISSING", response.get(0).getDocumentStatus());
        assertEquals(null, response.get(0).getFiles());
        assertEquals("COMPLETE", response.get(1).getDocumentStatus());
        assertEquals(1, response.get(1).getFiles().size());
        verify(itemFileRepository).findProfileItems("mobile-user-uuid");
        verify(itemFileRepository, never()).findFilesByItemCodes("mobile-user-uuid", List.of("MRI002"));
    }

    private com.seaman.entity.DocumentRequestItemEntity profileItem(
            String itemCode, String status, int fileUploaded, String profileRequestItemId) {
        com.seaman.entity.DocumentRequestItemEntity item = new com.seaman.entity.DocumentRequestItemEntity();
        item.setMobileUserUuid("mobile-user-uuid");
        item.setDocumentMasterRequestItemCode(itemCode);
        item.setStorageScope("PROFILE");
        item.setDocumentStatus(status);
        item.setFileUploaded(fileUploaded);
        item.setRequestItemFileUploaded(0);
        item.setProfileRequestItemId(profileRequestItemId);
        return item;
    }

    // ----------------------------------------------------------------------- helpers

    private DocumentEntity documentEntity() {
        DocumentEntity entity = new DocumentEntity();
        entity.setDocumentCode("DOC001");
        entity.setCertStartDate("2026-01-01");
        entity.setCertEndDate("2027-12-31");
        return entity;
    }

    private DocumentCreateRequest createRequest() {
        DocumentCreateRequest request = new DocumentCreateRequest();
        request.setDocumentCode("DOC001");
        request.setCertStartDate("2026-01-01");
        request.setCertEndDateType("A");
        request.setCertEndDate("2027-12-31");
        request.setFileCert("base64-file");
        request.setFileCertName("cert.png");
        return request;
    }

    private DocumentUpdateRequest updateRequest(String isChangeFile) {
        DocumentUpdateRequest request = new DocumentUpdateRequest();
        request.setIsChangeFile(isChangeFile);
        request.setDocumentCode("DOC001");
        request.setCertStartDate("2026-01-01");
        request.setCertEndDateType("A");
        request.setCertEndDate("2027-12-31");
        request.setFileCert("base64-file");
        request.setFileCertName("cert.png");
        return request;
    }
}
