package com.seaman.event;

import com.seaman.component.FcmSendNotificationComponent;
import com.seaman.entity.TransactionLogsEntity;
import com.seaman.repository.TransactionLogsRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Date;

@EnableAsync
@Component
@AllArgsConstructor
public class EventHandler {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
    private final FcmSendNotificationComponent fcmSendNotificationComponent;
    private final TransactionLogsRepository transactionLogsRepository;

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

    @Async
    @EventListener
    public void handleTransactionLogInsert(TransactionLogInsertEvent event) {
        try {
            TransactionLogsEntity entity = new TransactionLogsEntity();
            entity.setTransId(event.getTransId());
            entity.setRequestBy("MOBILE");
            entity.setServiceName(event.getServiceName());
            entity.setClientSessionId(event.getClientSessionId());
            entity.setLanguage(event.getLanguage());
            entity.setCorrelationId(event.getCorrelationId());
            entity.setDeviceModel(event.getDeviceModel());
            entity.setDeviceInfo(event.getDeviceInfo());
            entity.setToken(event.getToken());
            entity.setRequestData(event.getRequestData());
            entity.setRequestDateTime(new Date());
            entity.setCreateBy(event.getCreateBy());
            entity.setCreateDate(new Date());
            transactionLogsRepository.insert(entity);
        } catch (Exception ex) {
            log.error("TransactionLog insert event error -> {}", ex.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleTransactionLogUpdate(TransactionLogUpdateEvent event) {
        try {
            TransactionLogsEntity entity = new TransactionLogsEntity();
            entity.setTransId(event.getTransId());
            entity.setResponseData(event.getResponseData());
            entity.setResponseStatusCode(event.getStatusCode());
            entity.setResponseDateTime(new Date());
            entity.setUpdateBy(event.getUpdateBy());
            entity.setUpdateDate(new Date());

            if (event.getStatusMessage() != null) {
                entity.setResponseStatusMessage(event.getStatusMessage());
                transactionLogsRepository.updateStatusCodeMessage(entity);
            } else {
                transactionLogsRepository.update(entity);
            }
        } catch (Exception ex) {
            log.error("TransactionLog update event error -> {}", ex.getMessage());
        }
    }
}
