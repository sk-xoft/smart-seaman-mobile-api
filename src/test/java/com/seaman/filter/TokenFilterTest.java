package com.seaman.filter;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.service.JwtTokenService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenFilterTest {

    @Test
    void invalidTokenReturnsUnauthorizedWithoutExceptionDetail() throws Exception {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        MessageCodeService messageCodeService = mock(MessageCodeService.class);
        when(messageCodeService.getMessageDescription(AppStatus.JWT_EXPIRE, "TH"))
                .thenReturn("Token expired");
        when(jwtTokenService.parseClaims("expired-token"))
                .thenThrow(new BusinessException(AppStatus.JWT_EXPIRE, "ExpiredJwtException detail"));
        TokenFilter filter = new TokenFilter(jwtTokenService, messageCodeService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/profile");
        request.addHeader("Authorization", "Bearer expired-token");
        request.addHeader("Accept-Language", "TH");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertFalse(response.getContentAsString().contains("ExpiredJwtException detail"));
    }
}
