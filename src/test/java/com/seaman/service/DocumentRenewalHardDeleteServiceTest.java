package com.seaman.service;

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
class DocumentRenewalHardDeleteServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest httpServletRequest;

    private DocumentRenewalHardDeleteService service;
    private DocumentRenewalRequestEntity request;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalHardDeleteService(repository, httpServletRequest);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
        request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setStatusCode("PENDING_DOCUMENT_REVIEW");
    }

    @Test
    void hardDeletesOwnedRequestRegardlessOfStatus() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid")).thenReturn(request);

        DocumentRenewalDeleteResponse response = service.hardDelete("260700001");

        assertEquals("260700001", response.getRequestNo());
        assertEquals("PENDING_DOCUMENT_REVIEW", response.getFromStatus());
        assertEquals("DELETED", response.getToStatus());
        assertEquals("HARD_DELETE", response.getAction());
        verify(repository).hardDeleteRequest("request-id");
    }

    @Test
    void propagatesNotFoundForUnownedOrMissingRequest() {
        when(repository.lockOwnedRequestByNo("260700001", "user-uuid"))
                .thenThrow(new BusinessException(
                        com.seaman.constant.AppStatus.DATA_NOT_FOUND, "documentRenewalRequest"));

        assertThrows(BusinessException.class, () -> service.hardDelete("260700001"));
        verify(repository, never()).hardDeleteRequest(anyString());
    }

    @Test
    void rejectsInvalidRequestNoFormat() {
        assertThrows(BusinessException.class, () -> service.hardDelete("bad request no!!"));
        verifyNoInteractions(repository);
    }
}
