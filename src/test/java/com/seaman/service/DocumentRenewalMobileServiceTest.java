package com.seaman.service;

import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalMobileRequest;
import com.seaman.model.response.DocumentRenewalMobileResponse;
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
class DocumentRenewalMobileServiceTest {
    @Mock DocumentRenewalFoundationRepository repository;
    @Mock HttpServletRequest httpServletRequest;

    private DocumentRenewalMobileService service;
    private DocumentRenewalMobileRequest input;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalMobileService(repository, httpServletRequest);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
        input = new DocumentRenewalMobileRequest();
        input.setMobileNumber("0821549970");
    }

    @Test
    void updatesRequestAndDeliveryAddressMobileSnapshotForOwnedRequest() {
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setRequestNo("260700001");
        when(repository.lockOwnedRequestByNo("260700001", "mobile-user-uuid"))
                .thenReturn(request);

        DocumentRenewalMobileResponse response = service.update("260700001", input);

        assertEquals("260700001", response.getRequestNo());
        assertEquals("0821549970", response.getMobileNumber());
        verify(repository).lockOwnedRequestByNo("260700001", "mobile-user-uuid");
        verify(repository).updateRequestMobileNumber("request-id", "0821549970");
        verify(repository).updateDeliveryAddressMobileNumber(
                "request-id", "mobile-user-uuid", "0821549970");
    }

    @Test
    void rejectsUnauthenticatedUser() {
        when(httpServletRequest.getAttribute("userObject")).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.update("260700001", input));
        verifyNoInteractions(repository);
    }
}
