package com.seaman.service;

import com.seaman.entity.FcmNotificationEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.repository.FcmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmNotificationServiceTest {

    @Mock FcmRepository fcmRepository;
    @Mock TransactionLogsService transactionLogsService;
    @Mock HttpServletRequest httpServletRequest;

    private FcmNotificationService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new FcmNotificationService(fcmRepository, transactionLogsService);

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        user.setMobileUuid("mobile-user-uuid");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
    }

    @Test
    void fcmUpdateInsertsNewTokenWhenNoneExists() {
        when(fcmRepository.findByUserUUID("mobile-user-uuid")).thenReturn(null);

        String result = service.fcmUpdate(httpServletRequest, "new-fcm-token");

        assertEquals("crew@example.com update fcm token is success.", result);
        verify(fcmRepository).insert(argThat(entity ->
                "mobile-user-uuid".equals(entity.getUserMobile()) && "new-fcm-token".equals(entity.getTokenFcm())));
        verify(fcmRepository, never()).update(any());
    }

    @Test
    void fcmUpdateUpdatesExistingToken() {
        FcmNotificationEntity existing = new FcmNotificationEntity();
        existing.setUserMobile("mobile-user-uuid");
        existing.setTokenFcm("old-fcm-token");
        when(fcmRepository.findByUserUUID("mobile-user-uuid")).thenReturn(existing);

        String result = service.fcmUpdate(httpServletRequest, "new-fcm-token");

        assertEquals("crew@example.com update fcm token is success.", result);
        verify(fcmRepository).update(existing);
        assertEquals("new-fcm-token", existing.getTokenFcm());
        verify(fcmRepository, never()).insert(any());
    }
}
