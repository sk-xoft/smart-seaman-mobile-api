package com.seaman.service;

import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalStageResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalStageServiceTest {
    @Mock DocumentRenewalFoundationRepository foundationRepository;
    @Mock DocumentRenewalRepository renewalRepository;
    @Mock HttpServletRequest request;

    private DocumentRenewalStageService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalStageService(foundationRepository, renewalRepository, request);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
    }

    private List<DocumentRenewalStatusEntity> fullLadder() {
        return Arrays.asList(
                ladderRow("DOCUMENT_REVIEW", "ตรวจเอกสาร", "Document Review"),
                ladderRow("MARINE_DEPARTMENT_RESULT", "รอผลกรมเจ้าท่า", "Marine Department Result"),
                ladderRow("DEPARTMENT_DOCUMENT_PICKUP", "รับเอกสารจากกรมเจ้าท่า", "Department Document Pickup"),
                ladderRow("DELIVERING", "จัดส่ง", "Delivering"),
                ladderRow("DELIVERED", "จัดส่งสำเร็จ", "Delivered"));
    }

    private DocumentRenewalStatusEntity ladderRow(String mobileCode, String nameTh, String nameEn) {
        DocumentRenewalStatusEntity row = new DocumentRenewalStatusEntity();
        row.setDocumentMobileStatusCode(mobileCode);
        row.setDocumentMobileStatusNameTh(nameTh);
        row.setDocumentMobileStatusNameEn(nameEn);
        return row;
    }

    private DocumentRenewalRequestEntity ownedRequest(String mobileCode, String nameTh, String nameEn) {
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setDocumentMobileStatusCode(mobileCode);
        request.setDocumentMobileStatusNameTh(nameTh);
        request.setDocumentMobileStatusNameEn(nameEn);
        return request;
    }

    @Test
    void marksEarlierStepsDoneCurrentStepCurrentAndLaterStepsPending() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(ownedRequest("MARINE_DEPARTMENT_RESULT", "รอผลกรมเจ้าท่า", "Marine Department Result"));
        when(renewalRepository.findActiveStatuses()).thenReturn(fullLadder());

        DocumentRenewalStageResponse response = service.stage("260700001");

        assertEquals("260700001", response.getRequestNo());
        assertEquals(2, response.getCurrentStatus().getStep());
        assertEquals(5, response.getStages().size());
        assertEquals("DONE", response.getStages().get(0).getState());
        assertEquals("CURRENT", response.getStages().get(1).getState());
        assertEquals("PENDING", response.getStages().get(2).getState());
        assertEquals("PENDING", response.getStages().get(3).getState());
        assertEquals("PENDING", response.getStages().get(4).getState());
    }

    @Test
    void marksFirstStepCurrentWithNothingDone() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(ownedRequest("DOCUMENT_REVIEW", "ตรวจเอกสาร", "Document Review"));
        when(renewalRepository.findActiveStatuses()).thenReturn(fullLadder());

        DocumentRenewalStageResponse response = service.stage("260700001");

        assertEquals("CURRENT", response.getStages().get(0).getState());
        for (int i = 1; i < 5; i++) {
            assertEquals("PENDING", response.getStages().get(i).getState());
        }
    }

    @Test
    void marksAllEarlierStepsDoneWhenLastStepIsCurrent() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(ownedRequest("DELIVERED", "จัดส่งสำเร็จ", "Delivered"));
        when(renewalRepository.findActiveStatuses()).thenReturn(fullLadder());

        DocumentRenewalStageResponse response = service.stage("260700001");

        for (int i = 0; i < 4; i++) {
            assertEquals("DONE", response.getStages().get(i).getState());
        }
        assertEquals("CURRENT", response.getStages().get(4).getState());
    }

    @Test
    void marksAllStagesPendingWhenCurrentStatusIsOffTheLadder() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(ownedRequest(null, null, null));
        when(renewalRepository.findActiveStatuses()).thenReturn(fullLadder());

        DocumentRenewalStageResponse response = service.stage("260700001");

        assertNull(response.getCurrentStatus().getStep());
        for (int i = 0; i < 5; i++) {
            assertEquals("PENDING", response.getStages().get(i).getState());
        }
    }

    @Test
    void rejectsInvalidRequestNumberBeforeQuery() {
        assertThrows(BusinessException.class, () -> service.stage("invalid request"));
        verifyNoInteractions(foundationRepository);
        verifyNoInteractions(renewalRepository);
    }

    @Test
    void propagatesNotFoundWithoutFetchingLadder() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenThrow(new BusinessException("MA00016", "documentRenewalRequest"));

        assertThrows(BusinessException.class, () -> service.stage("260700001"));
        verifyNoInteractions(renewalRepository);
    }

    @Test
    void throwsWhenMasterLadderIsMissingAStep() {
        when(foundationRepository.findOwnedRequestByNo("260700001", "user-uuid"))
                .thenReturn(ownedRequest("DOCUMENT_REVIEW", "ตรวจเอกสาร", "Document Review"));
        when(renewalRepository.findActiveStatuses()).thenReturn(Collections.singletonList(
                ladderRow("DOCUMENT_REVIEW", "ตรวจเอกสาร", "Document Review")));

        assertThrows(BusinessException.class, () -> service.stage("260700001"));
    }
}
