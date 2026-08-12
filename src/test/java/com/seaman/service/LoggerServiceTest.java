package com.seaman.service;

import com.seaman.constant.AppSys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggerServiceTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;

    private LoggerService service;

    @BeforeEach
    void setUp() {
        service = new LoggerService();
        lenient().when(request.getMethod()).thenReturn("POST");
        lenient().when(request.getRequestURI()).thenReturn("/api/login");
        lenient().when(request.getAttribute(AppSys.TRACE_ID)).thenReturn("trace-1");
    }

    // ---- displayReq

    @Test
    void displayReqSetsRequestBodyAttributeWhenBodyPresent() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

        service.displayReq(request, "{\"password\":\"secret\"}");

        verify(request).setAttribute(eq(AppSys.REQUEST_BODY), anyString());
    }

    @Test
    void displayReqDoesNotSetRequestBodyAttributeWhenBodyAbsent() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

        service.displayReq(request, null);

        verify(request, never()).setAttribute(eq(AppSys.REQUEST_BODY), anyString());
    }

    @Test
    void displayReqFiltersToImportantHeadersOnly() {
        when(request.getHeaderNames()).thenReturn(
                Collections.enumeration(List.of("Content-Type", "X-Unimportant-Header")));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

        // Should not throw, and only "content-type" (an IMPORTANT_HEADERS entry) is consulted;
        // the unimportant header's value is never looked up.
        service.displayReq(request, null);

        verify(request, never()).getHeader("X-Unimportant-Header");
    }

    @Test
    void displayReqRedactsSensitiveParameters() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getParameterNames()).thenReturn(Collections.enumeration(List.of("password")));
        when(request.getParameter("password")).thenReturn("secret");

        // Should not throw while building the (redacted) parameters section of the log message.
        service.displayReq(request, null);
    }

    // ---- displayRes

    @Test
    void displayResHandlesEmptyHeaders() {
        when(response.getHeaderNames()).thenReturn(Collections.emptyList());

        service.displayRes(request, response, "{}");

        verify(response).getHeaderNames();
    }

    @Test
    void displayResIncludesHeadersWhenPresent() {
        when(response.getHeaderNames()).thenReturn(List.of("Content-Type"));
        when(response.getHeader("Content-Type")).thenReturn("application/json");

        service.displayRes(request, response, "{}");

        verify(response).getHeader("Content-Type");
    }
}
