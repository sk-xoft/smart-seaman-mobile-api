package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.FcmNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.event.DocumentRenewalResubmittedEvent;
import com.seaman.event.SendNotificationFcmEvent;
import com.seaman.repository.FcmRepository;
import com.seaman.repository.SendNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalNotificationServiceTest {
    @Mock SendNotificationRepository notificationRepository;
    @Mock FcmRepository fcmRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @Test
    void createsInAppNotificationAndPublishesFcmEvent() {
        DocumentRenewalNotificationService service = new DocumentRenewalNotificationService(
                notificationRepository, fcmRepository, eventPublisher);
        when(notificationRepository.insert(any())).thenReturn(25);
        when(notificationRepository.countNotificationByMUUID("user-uuid")).thenReturn(3);
        FcmNotificationEntity fcm = new FcmNotificationEntity();
        fcm.setTokenFcm("device-token");
        when(fcmRepository.findByUserUUID("user-uuid")).thenReturn(fcm);

        service.onResubmitted(new DocumentRenewalResubmittedEvent("user-uuid", "260700001"));

        ArgumentCaptor<SendNotificationEntity> notification =
                ArgumentCaptor.forClass(SendNotificationEntity.class);
        verify(notificationRepository).insert(notification.capture());
        assertEquals(AppSys.NOTI_TYPE_DOCUMENT_RENEWAL,
                notification.getValue().getNotiType());
        ArgumentCaptor<SendNotificationFcmEvent> event =
                ArgumentCaptor.forClass(SendNotificationFcmEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertEquals("260700001", event.getValue().getRequest().getData().getValueId());
    }

    @Test
    void keepsInAppNotificationWhenUserHasNoFcmToken() {
        DocumentRenewalNotificationService service = new DocumentRenewalNotificationService(
                notificationRepository, fcmRepository, eventPublisher);
        when(notificationRepository.insert(any())).thenReturn(25);

        service.onResubmitted(new DocumentRenewalResubmittedEvent("user-uuid", "260700001"));

        verify(notificationRepository).insert(any());
        verifyNoInteractions(eventPublisher);
    }
}
