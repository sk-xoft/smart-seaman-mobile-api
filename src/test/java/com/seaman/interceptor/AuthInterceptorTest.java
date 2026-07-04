package com.seaman.interceptor;

import com.seaman.constant.AppSys;
import com.seaman.entity.SessionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.repository.UserRepository;
import com.seaman.service.SessionService;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    @Test
    void authenticatesWithParsedJtiAndIndexedUserIdWithoutSessionWrite() throws Exception {
        SessionService sessionService = mock(SessionService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthInterceptor interceptor = new AuthInterceptor(sessionService, userRepository);

        SessionEntity session = new SessionEntity();
        session.setClientSessionId("session-1");
        session.setUserId("user-uuid");
        session.setIsOnline("YES");
        UsersEntity user = new UsersEntity();

        when(request.getHeader(AppSys.HEADER_AUTHORIZATION)).thenReturn("Bearer token");
        when(request.getAttribute(AppSys.JWT_JTI)).thenReturn("session-1");
        when(sessionService.findById("session-1")).thenReturn(session);
        when(userRepository.findByUserUID("user-uuid")).thenReturn(user);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(request).setAttribute("sessionObject", session);
        verify(request).setAttribute("userObject", user);
        verify(sessionService).findById("session-1");
        verifyNoMoreInteractions(sessionService);
        verify(userRepository).findByUserUID("user-uuid");
    }
}
