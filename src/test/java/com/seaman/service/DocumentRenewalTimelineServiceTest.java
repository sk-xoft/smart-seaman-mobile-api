package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalTransactionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalTimelineResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalTimelineServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest request;

    private DocumentRenewalTimelineService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalTimelineService(repository, request);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
        lenient().when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
    }

    @Test
    void mapsOwnedTimelineWithoutExposingInternalNote() {
        DocumentRenewalRequestEntity owned = new DocumentRenewalRequestEntity();
        owned.setId("request-id");
        when(repository.findOwnedRequestByNo("260700001", "user-uuid")).thenReturn(owned);
        DocumentRenewalTransactionEntity transaction = new DocumentRenewalTransactionEntity();
        transaction.setAction("SEND_BACK");
        transaction.setFromStatus("DOCUMENT_REVIEW");
        transaction.setToStatus("DOCUMENT_REVIEW");
        transaction.setNote("internal admin note");
        transaction.setActionedBy("admin-id");
        transaction.setActionedAt(Date.from(LocalDateTime.of(2026, 7, 9, 15, 45)
                .atZone(ZoneId.of("Asia/Bangkok")).toInstant()));
        when(repository.findOwnedTransactions("request-id", "user-uuid"))
                .thenReturn(Collections.singletonList(transaction));

        DocumentRenewalTimelineResponse response = service.timeline("260700001");

        assertEquals("260700001", response.getRequestNo());
        assertEquals("DOCUMENT_REVIEW", response.getItems().get(0).getFromStatus());
        assertEquals("DOCUMENT_REVIEW", response.getItems().get(0).getToStatus());
        assertEquals("09/07/2026 15:45", response.getItems().get(0).getActionedAt());
        assertEquals("ส่งเอกสารกลับเพื่อแก้ไข", response.getItems().get(0).getDetail());
        assertFalse(response.getItems().get(0).getDetail().contains("internal"));
    }

    @Test
    void mapsEnglishDisplayDetail() {
        when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("EN");
        DocumentRenewalRequestEntity owned = new DocumentRenewalRequestEntity();
        owned.setId("request-id");
        when(repository.findOwnedRequestByNo("260700001", "user-uuid")).thenReturn(owned);
        DocumentRenewalTransactionEntity transaction = new DocumentRenewalTransactionEntity();
        transaction.setAction("RESUBMIT");
        when(repository.findOwnedTransactions("request-id", "user-uuid"))
                .thenReturn(Collections.singletonList(transaction));

        assertEquals("Corrected documents resubmitted",
                service.timeline("260700001").getItems().get(0).getDetail());
    }

    @Test
    void rejectsInvalidRequestNumberBeforeQuery() {
        assertThrows(BusinessException.class, () -> service.timeline("invalid request"));
        verifyNoInteractions(repository);
    }
}
