package com.seaman.service;

import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalDeleteResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalDeleteServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest httpServletRequest;

    private DocumentRenewalDeleteService service;
    private DocumentRenewalRequestEntity request;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalDeleteService(repository, httpServletRequest);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
        request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
    }

    @Test
    void softDeletesUnpaidRequestAndAppendsCancelTransaction() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);

        DocumentRenewalDeleteResponse response = service.delete("260700001");

        assertEquals("260700001", response.getRequestNo());
        assertEquals("PAYMENT_PENDING", response.getFromStatus());
        assertEquals("CANCELLED", response.getToStatus());
        assertEquals("CANCEL", response.getAction());
        verify(repository).softDeleteRequest("request-id");
        verify(repository).appendTransaction("request-id", DocumentRenewalAction.CANCEL,
                DocumentRenewalStatus.PAYMENT_PENDING, DocumentRenewalStatus.CANCELLED,
                "Renewal request deleted by applicant", "user-uuid");
    }

    @Test
    void rejectsDeleteWhenRequestIsNotPaymentPending() {
        request.setStatusNameEn(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.getMasterNameEn());
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);

        assertThrows(BusinessException.class, () -> service.delete("260700001"));
        verify(repository, never()).softDeleteRequest(anyString());
        verify(repository, never()).appendTransaction(
                anyString(), any(), any(), any(), any(), any());
    }

    @Test
    void propagatesNotFoundForUnownedOrMissingRequest() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid"))
                .thenThrow(new BusinessException(
                        com.seaman.constant.AppStatus.DATA_NOT_FOUND, "documentRenewalRequest"));

        assertThrows(BusinessException.class, () -> service.delete("260700001"));
        verify(repository, never()).softDeleteRequest(anyString());
    }

    @Test
    void rejectsInvalidRequestNoFormat() {
        assertThrows(BusinessException.class, () -> service.delete("bad request no!!"));
        verifyNoInteractions(repository);
    }
}
