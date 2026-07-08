package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.FcmNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.event.DocumentRenewalResubmittedEvent;
import com.seaman.event.SendNotificationFcmEvent;
import com.seaman.model.external.request.FcmMessageData;
import com.seaman.model.external.request.FcmMessageRequest;
import com.seaman.model.request.NotificationModel;
import com.seaman.repository.FcmRepository;
import com.seaman.repository.SendNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRenewalNotificationService {
    private static final String TITLE = "Smart Seaman";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SendNotificationRepository notificationRepository;
    private final FcmRepository fcmRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResubmitted(DocumentRenewalResubmittedEvent event) {
        try {
            String body = "ส่งเอกสารคำขอ " + event.getRequestNo() + " เพื่อตรวจสอบอีกครั้งแล้ว";
            SendNotificationEntity notification = new SendNotificationEntity();
            notification.setMobileUserUUID(event.getMobileUserUuid());
            notification.setTerm("-");
            notification.setTitleMessage(TITLE);
            notification.setBodyMessage(body);
            notification.setSuccess("0");
            notification.setFailure("0");
            notification.setNotiType(AppSys.NOTI_TYPE_DOCUMENT_RENEWAL);
            notification.setReadStatus("NO");
            int notificationId = notificationRepository.insert(notification);

            FcmNotificationEntity fcm = fcmRepository.findByUserUUID(event.getMobileUserUuid());
            if (fcm == null || fcm.getTokenFcm() == null || fcm.getTokenFcm().trim().isEmpty()) {
                return;
            }
            int count = notificationRepository.countNotificationByMUUID(event.getMobileUserUuid());
            FcmMessageData data = new FcmMessageData();
            data.setTitle(TITLE);
            data.setBody(body);
            data.setNotiType(AppSys.NOTI_TYPE_DOCUMENT_RENEWAL);
            data.setCountNoti(String.valueOf(count));
            data.setNotiId(String.valueOf(notificationId));
            data.setValueId(event.getRequestNo());

            NotificationModel notificationModel = new NotificationModel();
            notificationModel.setTitle(TITLE);
            notificationModel.setBody(body);
            notificationModel.setSound("default");
            notificationModel.setBadge(String.valueOf(count));

            FcmMessageRequest request = new FcmMessageRequest();
            request.setData(data);
            request.setTo(fcm.getTokenFcm());
            request.setPriority("high");
            request.setMutable_content(true);
            request.setNotification(notificationModel);
            eventPublisher.publishEvent(new SendNotificationFcmEvent(
                    this, List.of(fcm.getTokenFcm()), request));
        } catch (Exception ex) {
            log.error("Cannot create document renewal resubmit notification", ex);
        }
    }
}
