package com.seaman.event;

import com.seaman.component.FcmSendNotificationComponent;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@Component
@AllArgsConstructor
public class EventHandler {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
    private final FcmSendNotificationComponent fcmSendNotificationComponent;

    @Async
    @SneakyThrows
    @EventListener
    public void handleSendFcmNotificationEvent(SendNotificationFcmEvent event) {

        if (event.getRequest() != null && !event.getDeviceTokens().isEmpty()) {
            fcmSendNotificationComponent.senderNotification(
                    event.getDeviceTokens(),
                    event.getRequest()
            );
            log.info("Event send fcm is success.  info {}", event.getRequest().getTo());
        } else {
            log.info("Event send fcm data is empty.");
        }
    }
}
