package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.SessionEntity;
import com.seaman.event.TransactionLogInsertEvent;
import com.seaman.event.TransactionLogUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionLogsServiceTest {

    @Mock ApplicationEventPublisher eventPublisher;
    @Mock HttpServletRequest httpServletRequest;

    private TransactionLogsService service;

    @BeforeEach
    void setUp() {
        service = new TransactionLogsService(eventPublisher, httpServletRequest);
    }

    // ---- insert

    @Test
    void insertPublishesEventWithRequestContextAndMaskedAuthorization() {
        when(httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE)).thenReturn("EN");
        when(httpServletRequest.getHeader(AppSys.HEADER_DEVICE_MODEL)).thenReturn("iPhone");
        when(httpServletRequest.getHeader(AppSys.HEADER_DEVICE_INFO)).thenReturn("iOS 17");
        when(httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID)).thenReturn("corr-1");
        when(httpServletRequest.getHeader(AppSys.HEADER_AUTHORIZATION)).thenReturn("Bearer secret-token");
        SessionEntity session = new SessionEntity();
        session.setClientSessionId("session-1");
        when(httpServletRequest.getAttribute("sessionObject")).thenReturn(session);

        service.insert("trans-1", "{\"a\":1}", "LOGIN", "crew@example.com");

        ArgumentCaptor<TransactionLogInsertEvent> captor = ArgumentCaptor.forClass(TransactionLogInsertEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        TransactionLogInsertEvent event = captor.getValue();
        assertEquals("trans-1", event.getTransId());
        assertEquals("LOGIN", event.getServiceName());
        assertEquals("crew@example.com", event.getCreateBy());
        assertEquals("EN", event.getLanguage());
        assertEquals("iPhone", event.getDeviceModel());
        assertEquals("iOS 17", event.getDeviceInfo());
        assertEquals("corr-1", event.getCorrelationId());
        assertEquals("(protected)", event.getToken());
        assertEquals("session-1", event.getClientSessionId());
    }

    @Test
    void insertLeavesTokenEmptyWhenAuthorizationHeaderMissing() {
        when(httpServletRequest.getAttribute("sessionObject")).thenReturn(null);
        when(httpServletRequest.getAttribute("clientSessionId")).thenReturn("fallback-session");

        service.insert("trans-1", "{}", "LOGIN", "crew@example.com");

        ArgumentCaptor<TransactionLogInsertEvent> captor = ArgumentCaptor.forClass(TransactionLogInsertEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals("", captor.getValue().getToken());
        assertEquals("fallback-session", captor.getValue().getClientSessionId());
    }

    @Test
    void insertSwallowsExceptionFromPublisher() {
        when(httpServletRequest.getAttribute("sessionObject")).thenReturn(null);
        doThrow(new RuntimeException("publish failed")).when(eventPublisher).publishEvent(any());

        service.insert("trans-1", "{}", "LOGIN", "crew@example.com");

        // No exception propagates out of insert().
        verify(eventPublisher).publishEvent(any());
    }

    // ---- update

    @Test
    void updatePublishesEventWithStatusCodeAndNullStatusMessage() {
        service.update("trans-1", "{}", "MA00000", "crew@example.com");

        ArgumentCaptor<TransactionLogUpdateEvent> captor = ArgumentCaptor.forClass(TransactionLogUpdateEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        TransactionLogUpdateEvent event = captor.getValue();
        assertEquals("trans-1", event.getTransId());
        assertEquals("MA00000", event.getStatusCode());
        assertEquals("crew@example.com", event.getUpdateBy());
        assertNull(event.getStatusMessage());
    }

    @Test
    void updateSwallowsExceptionFromPublisher() {
        doThrow(new RuntimeException("publish failed")).when(eventPublisher).publishEvent(any());

        service.update("trans-1", "{}", "MA00000", "crew@example.com");

        verify(eventPublisher).publishEvent(any());
    }

    // ---- updateStatusMessage

    @Test
    void updateStatusMessagePublishesEventWithStatusMessageAndNullUpdateBy() {
        service.updateStatusMessage("trans-1", "{}", "MA99999", "boom");

        ArgumentCaptor<TransactionLogUpdateEvent> captor = ArgumentCaptor.forClass(TransactionLogUpdateEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        TransactionLogUpdateEvent event = captor.getValue();
        assertEquals("MA99999", event.getStatusCode());
        assertEquals("boom", event.getStatusMessage());
        assertNull(event.getUpdateBy());
    }
}
