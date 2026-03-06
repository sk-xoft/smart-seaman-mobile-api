package com.seaman.service;

import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.MSendNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.entity.UserSendNotificationEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.external.request.FcmMessageData;
import com.seaman.model.external.request.FcmMessageRequest;
import com.seaman.model.external.response.FcmMessageResponse;
import com.seaman.model.request.MSendNotificationsRequest;
import com.seaman.model.request.NotificationModel;
import com.seaman.model.request.SendNotificationRequest;
import com.seaman.model.response.NotificationResponse;
import com.seaman.model.response.NotificationsModel;
import com.seaman.model.response.UpdateNotificationsResponse;
import com.seaman.repository.SendNotificationRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.ExternalApiUtils;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendNotificationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SendNotificationRepository sendNotificationRepository;

    private final TransactionLogsService transactionLogsService;

    private final ExternalApiUtils externalApiUtils;

    private final FrameworkUtils frameworkUtils;

    private final DateUtil dateUtil;

    @Value("${fcm.url}")
    private String fcmNotificationUrl;

    @Value("${fcm.auth}")
    private String fcmAuth;

    private String titleMessage = "Smart Seaman";

    private String bodyMessage = "ประกาศนียบัตรใกล้จะหมดอายุ";

    public void sendNotification() {

        try {

            List<UserSendNotificationEntity> userSendNotificationEntities = sendNotificationRepository.listUserSendNotifications();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", String.format("key=%s", fcmAuth));

            for (UserSendNotificationEntity item : userSendNotificationEntities) {

                // Insert log send notification
                SendNotificationEntity entity = new SendNotificationEntity();
                entity.setMobileUserUUID(item.getUserMobileUuid());
                entity.setTerm("-");
                entity.setTitleMessage(titleMessage);
                entity.setBodyMessage(bodyMessage);
                entity.setSuccess("0");
                entity.setFailure("0");
                entity.setNotiType(AppSys.NOTI_TYPE_CERT_EXPIRED);
                entity.setReadStatus("NO");

                int tableId = sendNotificationRepository.insert(entity);
                int countNoti = sendNotificationRepository.countNotificationByMUUID(item.getUserMobileUuid());

                FcmMessageData fcmMessageData = new FcmMessageData();
                fcmMessageData.setTitle(titleMessage);
                fcmMessageData.setBody(bodyMessage);
                fcmMessageData.setNotiType(AppSys.NOTI_TYPE_CERT_EXPIRED);
                fcmMessageData.setCountNoti(String.valueOf(countNoti));
                fcmMessageData.setNotiId(String.valueOf(tableId));
                fcmMessageData.setValueId(String.valueOf(tableId));

                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setTitle(titleMessage);
                notificationModel.setBody(bodyMessage);
                notificationModel.setSound("default");
                notificationModel.setBadge(String.valueOf(countNoti));

                // Prepare message
                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
                fcmMessageRequest.setData(fcmMessageData);

                // Prepare token fcm
                fcmMessageRequest.setTo(item.getTokenFcm());
                fcmMessageRequest.setPriority("high");
                fcmMessageRequest.setMutable_content(true);
                fcmMessageRequest.setNotification(notificationModel);

                FcmMessageResponse fcmMessageResponse = (FcmMessageResponse) externalApiUtils.commonCaller(fcmNotificationUrl,
                        HttpMethod.POST, headers, fcmMessageRequest, FcmMessageRequest.class, FcmMessageResponse.class,
                        Object.class);

                Gson gson = new Gson();
                String bodyResponse = gson.toJson(fcmMessageResponse, FcmMessageResponse.class);
                entity.setResponseBodyFcm(bodyResponse);

                entity.setSuccess(fcmMessageResponse.getSuccess());
                entity.setFailure(fcmMessageResponse.getFailure());

                sendNotificationRepository.updateNotificationById(String.valueOf(tableId), entity);
            }

            log.info("Send notification is success. User list -> {} ", userSendNotificationEntities.size());
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    public NotificationResponse notifications(HttpServletRequest httpServletRequest) {

        NotificationResponse response = new NotificationResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GETTED NOTIFICATIONS";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<NotificationsModel> modelList = new ArrayList<>();
            for (MSendNotificationEntity item : sendNotificationRepository.listMsendNotifications(usersEntity.getMobileUuid())) {
                NotificationsModel model = new NotificationsModel();
                model.setId(item.getId());
                model.setReadStatus(item.getReadStatus());
                model.setReadDate(item.getReadDate());
                model.setNotiType(item.getNotiType());
                model.setMobileUserUuid(item.getMobileUserUuid());
                model.setTitleMessage(item.getTitleMessage());
                model.setBodyMessage(item.getBodyMessage());
                model.setSuccess(item.getSuccess());
                model.setReadDate(item.getReadDate());
                model.setRetry(item.getRetry());

                String notiDate = "";

                if (null != item.getNotiDate()) {
                    notiDate = dateUtil.formatStrToStrDDMMYYYY(item.getNotiDate(), DateUtil.YEAR_MONTH_DATE);
                }

                model.setNotiDate(notiDate);
                model.setValueId(item.getValueId());
                modelList.add(model);
            }

            response.setNotificationsModels(modelList);
            log.info("Get M send List notifications is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public UpdateNotificationsResponse updateNotifications(HttpServletRequest httpServletRequest, MSendNotificationsRequest request) {

        UpdateNotificationsResponse response = new UpdateNotificationsResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATED NOTIFICATIONS";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if(!request.getNotiId().equals("0")){

                // Update Notification
                boolean isUPdate = sendNotificationRepository.updateNotificationById(request.getNotiId());

                if (!isUPdate) {
                    log.error("Can't update notifications.");
                    throw new BusinessException(AppStatus.DATA_NOT_FOUND, "");
                }
            }

            // Count Notification
            Integer countNotification = sendNotificationRepository.countNotificationByMUUID(usersEntity.getMobileUuid());
            response.setCountNotification(String.valueOf(countNotification));

            log.info("Update notifications is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }


    public UpdateNotificationsResponse updateAllNotifications(HttpServletRequest httpServletRequest) {

        UpdateNotificationsResponse response = new UpdateNotificationsResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NOTIFICATIONS CLEARED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Update Notification
            sendNotificationRepository.updateNotificationByUUID(usersEntity.getMobileUuid());

            // Count Notification
            Integer countNotification = sendNotificationRepository.countNotificationByMUUID(usersEntity.getMobileUuid());
            response.setCountNotification(String.valueOf(countNotification));

            log.info("Update notifications is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }

    public UpdateNotificationsResponse updateNotificationsByValueId(HttpServletRequest httpServletRequest, SendNotificationRequest request) {

        UpdateNotificationsResponse response = new UpdateNotificationsResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE NOTIFICATIONS BY VALUE ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Find noti.
            List<MSendNotificationEntity> mSendNotificationEntities = sendNotificationRepository.findByUserUuidAndNotiTypeAndValueId(usersEntity.getMobileUuid(), request.getValueId(),  request.getNotiType());

            if(!mSendNotificationEntities.isEmpty()) {

                MSendNotificationEntity item = mSendNotificationEntities.get(0);
                boolean isUpdate = sendNotificationRepository.updateNotificationById(item.getId());
                if(isUpdate) {
                    log.info("Update noti by value id and  noti type is success.");
                }
            }

            // Count Notification
            Integer countNotification = sendNotificationRepository.countNotificationByMUUID(usersEntity.getMobileUuid());
            response.setCountNotification(String.valueOf(countNotification));
            log.info("Update notifications is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }
}
