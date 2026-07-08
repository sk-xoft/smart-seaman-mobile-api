package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppSys;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DeliveryEntity;
import com.seaman.entity.DeptSubmissionEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalSummaryEntity;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.repository.DocumentRenewalDetailRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalDetailServiceTest {
    @Mock DocumentRenewalFoundationRepository foundationRepository;
    @Mock DocumentRenewalDetailRepository detailRepository;
    @Mock DocumentRequestItemFileRepository fileRepository;
    @Mock AmazonS3 s3;
    @Mock HttpServletRequest request;

    private DocumentRenewalDetailService service;
    private DocumentRenewalRequestEntity owned;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalDetailService(
                foundationRepository, detailRepository, fileRepository, s3, request);
        ReflectionTestUtils.setField(service, "bucketName", "bucket");
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
        lenient().when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");

        owned = new DocumentRenewalRequestEntity();
        owned.setId("request-id");
        owned.setRequestNo("260700001");
        owned.setMobileUserUuid("user-uuid");
        owned.setDocumentCode("DOC001");
        owned.setDocumentStatusId("status-id");
        owned.setStatusNameTh("รอผู้ยื่นแก้ไข");
        owned.setStatusNameEn(
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn());
        owned.setStatusCssColor("#ff914d");
        owned.setAmount(new BigDecimal("1500.00"));
    }

    @Test
    void mapsCorrectionItemsWithShortLivedFileAccessAndNoFutureDetail() throws Exception {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(owned);
        DocumentRenewalSummaryEntity document = new DocumentRenewalSummaryEntity();
        document.setDocumentNameTh("ประกาศนียบัตร");
        when(detailRepository.findDocumentNames("DOC001")).thenReturn(document);
        RenewalRequestItemEntity item = new RenewalRequestItemEntity();
        item.setId("item-id");
        item.setDocumentMasterRequestItemCode("MRI002");
        item.setDocumentNameTh("รูปถ่าย");
        item.setSortOrder(2);
        item.setApproveStatus("FIX");
        item.setNote("รูปไม่ชัด");
        when(foundationRepository.findOwnedRequestItems("request-id", "user-uuid"))
                .thenReturn(Collections.singletonList(item));
        DocumentRequestItemFileEntity file = new DocumentRequestItemFileEntity();
        file.setId("file-id");
        file.setStorageKey("documents/user/file-id");
        file.setFileUploaded(1);
        file.setIsUpdated(true);
        when(fileRepository.findFiles("user-uuid", "MRI002"))
                .thenReturn(Collections.singletonList(file));
        when(s3.generatePresignedUrl(eq("bucket"), eq("documents/user/file-id"), any(Date.class)))
                .thenReturn(new URL("https://storage.example/signed"));

        DocumentRenewalDetailResponse response = service.detail("260700001");

        assertEquals("ประกาศนียบัตร", response.getDocumentName());
        assertEquals("fix", response.getItems().get(0).getCheckResult());
        assertEquals("รูปไม่ชัด", response.getItems().get(0).getCheckNote());
        assertTrue(response.getItems().get(0).getIsUpdated());
        assertEquals("https://storage.example/signed",
                response.getItems().get(0).getFiles().get(0).getFileUrl());
        assertNull(response.getDeptSubmission());
        assertNull(response.getDelivery());
        verify(foundationRepository, never()).findOwnedDeptSubmissions(anyString(), anyString());
        verify(foundationRepository, never()).findOwnedDeliveries(anyString(), anyString());
    }

    @Test
    void exposesDepartmentAndDeliveryOnlyForDeliveryStatus() {
        owned.setStatusNameEn(DocumentRenewalStatus.DELIVERING.getMasterNameEn());
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(owned);
        when(foundationRepository.findOwnedRequestItems("request-id", "user-uuid"))
                .thenReturn(Collections.emptyList());
        DeptSubmissionEntity department = new DeptSubmissionEntity();
        department.setSubmittedToDeptDate(LocalDate.of(2026, 7, 1));
        when(foundationRepository.findOwnedDeptSubmissions("request-id", "user-uuid"))
                .thenReturn(Collections.singletonList(department));
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setTrackingNo("TH123");
        when(foundationRepository.findOwnedDeliveries("request-id", "user-uuid"))
                .thenReturn(Collections.singletonList(delivery));

        DocumentRenewalDetailResponse response = service.detail("260700001");

        assertEquals("01/07/2026", response.getDeptSubmission().getSubmittedToDeptDate());
        assertEquals("TH123", response.getDelivery().getTrackingNo());
    }

    @Test
    void rejectsInvalidRequestNumberBeforeReadingData() {
        assertThrows(BusinessException.class, () -> service.detail("invalid request"));
        verifyNoInteractions(foundationRepository, detailRepository, fileRepository, s3);
    }
}
