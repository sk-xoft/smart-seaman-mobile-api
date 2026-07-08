package com.seaman.service;

import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.event.DocumentRenewalResubmittedEvent;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalResubmitServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest httpServletRequest;
    @Mock ApplicationEventPublisher eventPublisher;

    private DocumentRenewalResubmitService service;
    private DocumentRenewalRequestEntity request;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalResubmitService(repository, httpServletRequest, eventPublisher);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
        request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setDocumentStatusId("correction-status-id");
        request.setStatusNameEn(
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION.getMasterNameEn());
    }

    @Test
    void atomicallyResubmitsCompletedCorrectionsAndPublishesEvent() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);
        when(repository.countFixItems("request-id")).thenReturn(2);
        when(repository.countIncompleteCorrectedFixItems("request-id", "user-uuid")).thenReturn(0);
        when(repository.findActiveStatusId(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW))
                .thenReturn("review-status-id");

        DocumentRenewalResubmitResponse response = service.resubmit("260700001");

        assertEquals("260700001", response.getRequestNo());
        assertEquals("RESUBMIT", response.getAction());
        verify(repository).resetFixItemsForReview("request-id");
        verify(repository).updateStatus("request-id", "correction-status-id", "review-status-id");
        verify(repository).appendTransaction("request-id", DocumentRenewalAction.RESUBMIT,
                DocumentRenewalStatus.PENDING_APPLICANT_CORRECTION,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                "Corrected documents resubmitted", "user-uuid");
        ArgumentCaptor<DocumentRenewalResubmittedEvent> event =
                ArgumentCaptor.forClass(DocumentRenewalResubmittedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertEquals("260700001", event.getValue().getRequestNo());
    }

    @Test
    void rejectsIncompleteCorrectionsWithoutTransition() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);
        when(repository.countFixItems("request-id")).thenReturn(1);
        when(repository.countIncompleteCorrectedFixItems("request-id", "user-uuid")).thenReturn(1);

        assertThrows(BusinessException.class, () -> service.resubmit("260700001"));
        verify(repository, never()).updateStatus(anyString(), anyString(), anyString());
        verify(repository, never()).appendTransaction(anyString(), any(), any(), any(), any(), any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsDoubleSubmitFromWrongState() {
        request.setStatusNameEn(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.getMasterNameEn());
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);

        assertThrows(BusinessException.class, () -> service.resubmit("260700001"));
        verify(repository, never()).countFixItems(anyString());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsCorrectionStateWithoutFixItems() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);
        when(repository.countFixItems("request-id")).thenReturn(0);

        assertThrows(BusinessException.class, () -> service.resubmit("260700001"));
        verify(repository, never()).resetFixItemsForReview(anyString());
        verifyNoInteractions(eventPublisher);
    }
}
