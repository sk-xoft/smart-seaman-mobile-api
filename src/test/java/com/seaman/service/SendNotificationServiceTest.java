package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.MSendNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.entity.UserSendNotificationEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.event.SendNotificationFcmEvent;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.MSendNotificationsRequest;
import com.seaman.model.request.SendNotificationRequest;
import com.seaman.model.response.NotificationResponse;
import com.seaman.model.response.UpdateNotificationsResponse;
import com.seaman.repository.SendNotificationRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock SendNotificationRepository sendNotificationRepository;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock DateUtil dateUtil;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock HttpServletRequest httpServletRequest;

    private SendNotificationService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new SendNotificationService(sendNotificationRepository, transactionLogsService,
                frameworkUtils, dateUtil, eventPublisher);

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        user.setMobileUuid("mobile-user-uuid");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- sendNotification

    @Test
    void sendNotificationPublishesFcmEventPerUser() {
        UserSendNotificationEntity item = new UserSendNotificationEntity();
        item.setUserMobileUuid("mobile-user-uuid");
        item.setTokenFcm("fcm-token-1");
        when(sendNotificationRepository.listUserSendNotifications()).thenReturn(List.of(item));
        when(sendNotificationRepository.insert(any(SendNotificationEntity.class))).thenReturn(101);
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(3);

        service.sendNotification();

        ArgumentCaptor<SendNotificationFcmEvent> captor = ArgumentCaptor.forClass(SendNotificationFcmEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        SendNotificationFcmEvent event = captor.getValue();
        assertEquals(List.of("fcm-token-1"), event.getDeviceTokens());
        assertEquals("fcm-token-1", event.getRequest().getTo());
        assertEquals("3", event.getRequest().getData().getCountNoti());
        assertEquals("101", event.getRequest().getData().getNotiId());
    }

    @Test
    void sendNotificationSwallowsExceptionFromRepository() {
        when(sendNotificationRepository.listUserSendNotifications()).thenThrow(new RuntimeException("db down"));

        service.sendNotification();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void sendNotificationHandlesEmptyUserList() {
        when(sendNotificationRepository.listUserSendNotifications()).thenReturn(Collections.emptyList());

        service.sendNotification();

        verify(eventPublisher, never()).publishEvent(any());
    }

    // ---- notifications

    @Test
    void notificationsMapsListAndFormatsNotiDateWhenPresent() {
        MSendNotificationEntity item = new MSendNotificationEntity();
        item.setId("1");
        item.setNotiDate("2026-08-01");
        when(sendNotificationRepository.listMsendNotifications("mobile-user-uuid")).thenReturn(List.of(item));
        when(dateUtil.formatStrToStrDDMMYYYY("2026-08-01", DateUtil.YEAR_MONTH_DATE)).thenReturn("01/08/2026");

        NotificationResponse response = service.notifications(httpServletRequest);

        assertEquals(1, response.getNotificationsModels().size());
        assertEquals("01/08/2026", response.getNotificationsModels().get(0).getNotiDate());
    }

    @Test
    void notificationsLeavesNotiDateEmptyWhenNull() {
        MSendNotificationEntity item = new MSendNotificationEntity();
        item.setId("1");
        item.setNotiDate(null);
        when(sendNotificationRepository.listMsendNotifications("mobile-user-uuid")).thenReturn(List.of(item));

        NotificationResponse response = service.notifications(httpServletRequest);

        assertEquals("", response.getNotificationsModels().get(0).getNotiDate());
        verify(dateUtil, never()).formatStrToStrDDMMYYYY(any(), any());
    }

    // ---- updateNotifications

    @Test
    void updateNotificationsUpdatesGivenId() {
        MSendNotificationsRequest req = new MSendNotificationsRequest();
        req.setNotiId("5");
        when(sendNotificationRepository.updateNotificationById("5")).thenReturn(true);
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(2);

        UpdateNotificationsResponse response = service.updateNotifications(httpServletRequest, req);

        assertEquals("2", response.getCountNotification());
    }

    @Test
    void updateNotificationsSkipsUpdateWhenIdIsZero() {
        MSendNotificationsRequest req = new MSendNotificationsRequest();
        req.setNotiId("0");
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(0);

        UpdateNotificationsResponse response = service.updateNotifications(httpServletRequest, req);

        assertEquals("0", response.getCountNotification());
        verify(sendNotificationRepository, never()).updateNotificationById(anyString());
    }

    @Test
    void updateNotificationsThrowsWhenUpdateFails() {
        MSendNotificationsRequest req = new MSendNotificationsRequest();
        req.setNotiId("5");
        when(sendNotificationRepository.updateNotificationById("5")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateNotifications(httpServletRequest, req));
        assertEquals(AppStatus.DATA_NOT_FOUND, ex.getCode());
    }

    // ---- updateAllNotifications

    @Test
    void updateAllNotificationsClearsAndReturnsCount() {
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(0);

        UpdateNotificationsResponse response = service.updateAllNotifications(httpServletRequest);

        assertEquals("0", response.getCountNotification());
        verify(sendNotificationRepository).updateNotificationByUUID("mobile-user-uuid");
    }

    // ---- updateNotificationsByValueId

    @Test
    void updateNotificationsByValueIdUpdatesFirstMatchWhenPresent() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setValueId("101");
        req.setNotiType("CERT_EXPIRED");
        MSendNotificationEntity match = new MSendNotificationEntity();
        match.setId("55");
        when(sendNotificationRepository.findByUserUuidAndNotiTypeAndValueId("mobile-user-uuid", "101", "CERT_EXPIRED"))
                .thenReturn(List.of(match));
        when(sendNotificationRepository.updateNotificationById("55")).thenReturn(true);
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(1);

        UpdateNotificationsResponse response = service.updateNotificationsByValueId(httpServletRequest, req);

        assertEquals("1", response.getCountNotification());
        verify(sendNotificationRepository).updateNotificationById("55");
    }

    @Test
    void updateNotificationsByValueIdSkipsUpdateWhenNoMatch() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setValueId("101");
        req.setNotiType("CERT_EXPIRED");
        when(sendNotificationRepository.findByUserUuidAndNotiTypeAndValueId("mobile-user-uuid", "101", "CERT_EXPIRED"))
                .thenReturn(Collections.emptyList());
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(0);

        UpdateNotificationsResponse response = service.updateNotificationsByValueId(httpServletRequest, req);

        assertEquals("0", response.getCountNotification());
        verify(sendNotificationRepository, never()).updateNotificationById(anyString());
    }
}
