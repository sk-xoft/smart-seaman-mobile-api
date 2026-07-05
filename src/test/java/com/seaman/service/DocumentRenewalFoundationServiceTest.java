package com.seaman.service;

import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalFoundationServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest httpServletRequest;

    private DocumentRenewalFoundationService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalFoundationService(repository, httpServletRequest);
        user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
    }

    @Test
    void ownershipAlwaysComesFromAuthenticatedUser() {
        String requestId = UUID.randomUUID().toString();
        DocumentRenewalRequestEntity entity = new DocumentRenewalRequestEntity();
        when(repository.findOwnedRequest(requestId, "mobile-user-uuid")).thenReturn(entity);

        assertEquals(entity, service.requireOwnedRequest(requestId));
        verify(repository).findOwnedRequest(requestId, "mobile-user-uuid");
    }

    @Test
    void statusUpdateAndTimelineArePerformedTogether() {
        String requestId = UUID.randomUUID().toString();
        DocumentRenewalRequestEntity entity = new DocumentRenewalRequestEntity();
        entity.setDocumentStatusId("from-status-id");
        entity.setStatusNameEn(DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn());
        when(repository.lockOwnedRequest(requestId, "mobile-user-uuid")).thenReturn(entity);
        when(repository.findActiveStatusId(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW))
                .thenReturn("to-status-id");

        service.transitionOwnedRequest(requestId,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                DocumentRenewalAction.RESUBMIT, "corrected");

        verify(repository).updateStatus(requestId, "from-status-id", "to-status-id");
        verify(repository).appendTransaction(requestId, DocumentRenewalAction.RESUBMIT,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                "corrected", "mobile-user-uuid");
    }

    @Test
    void rejectsTransitionFromUnexpectedStatusWithoutWriting() {
        String requestId = UUID.randomUUID().toString();
        DocumentRenewalRequestEntity entity = new DocumentRenewalRequestEntity();
        entity.setStatusNameEn(DocumentRenewalStatus.DELIVERING.getMasterNameEn());
        when(repository.lockOwnedRequest(requestId, "mobile-user-uuid")).thenReturn(entity);

        assertThrows(BusinessException.class, () -> service.transitionOwnedRequest(requestId,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                DocumentRenewalAction.RESUBMIT, null));
        verify(repository, never()).updateStatus(anyString(), anyString(), anyString());
        verify(repository, never()).appendTransaction(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsInvalidRequestIdBeforeQuery() {
        assertThrows(BusinessException.class, () -> service.requireOwnedRequest("not-a-uuid"));
        verifyNoInteractions(repository);
    }
}
