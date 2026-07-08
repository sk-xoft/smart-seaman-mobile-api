package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.DocumentRenewalSummaryEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.PageDocumentRenewalResponse;
import com.seaman.repository.DocumentRenewalListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalListServiceTest {
    @Mock DocumentRenewalListRepository repository;
    @Mock HttpServletRequest request;

    private DocumentRenewalListService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalListService(repository, request);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
        lenient().when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
    }

    @Test
    void mapsOwnedPageAndComputesLastPage() {
        DocumentRenewalSummaryEntity row = new DocumentRenewalSummaryEntity();
        row.setRequestId("request-id");
        row.setRequestNo("260700001");
        row.setDocumentCode("DOC001");
        row.setDocumentNameTh("ประกาศนียบัตร");
        row.setDocumentNameEn("Certificate");
        row.setStatusId("status-id");
        row.setStatusNameTh("รอตรวจเอกสาร");
        row.setStatusNameEn("Pending Document Review");
        row.setStatusCssColor("#ff0000");
        row.setSubmittedAt(Date.from(LocalDateTime.of(2026, 7, 9, 14, 30)
                .atZone(ZoneId.of("Asia/Bangkok")).toInstant()));
        row.setAmount(new BigDecimal("1500.00"));
        row.setIsResubmit(true);
        when(repository.countByUser("user-uuid")).thenReturn(11);
        when(repository.findByUser("user-uuid", 10)).thenReturn(Collections.singletonList(row));

        PageDocumentRenewalResponse response = service.listMyRequests(10);

        assertEquals(11, response.getItemTotal());
        assertTrue(response.isLast());
        assertEquals("09/07/2026 14:30", response.getItems().get(0).getSubmittedAt());
        assertEquals("ประกาศนียบัตร", response.getItems().get(0).getDocumentName());
        assertEquals(1, response.getItems().get(0).getStatus().getStep());
        assertTrue(response.getItems().get(0).getIsResubmit());
        verify(repository).findByUser("user-uuid", 10);
    }

    @Test
    void marksFullNonFinalPage() {
        when(repository.countByUser("user-uuid")).thenReturn(25);
        when(repository.findByUser("user-uuid", 0)).thenReturn(Collections.nCopies(
                10, new DocumentRenewalSummaryEntity()));

        assertFalse(service.listMyRequests(0).isLast());
    }

    @Test
    void rejectsNegativeOffsetBeforeQuery() {
        assertThrows(BusinessException.class, () -> service.listMyRequests(-1));
        verifyNoInteractions(repository);
    }
}
