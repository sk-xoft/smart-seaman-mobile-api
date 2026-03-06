package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.FcmNotificationEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.CommonException;
import com.seaman.repository.FcmRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final FcmRepository fcmRepository;
    private final TransactionLogsService transactionLogsService;


    public String fcmUpdate(HttpServletRequest httpServletRequest, String fcmToken) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATED FCM";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            FcmNotificationEntity entity = fcmRepository.findByUserUUID(usersEntity.getMobileUuid());

            if (null == entity) {
                entity = new FcmNotificationEntity();
                entity.setUserMobile(usersEntity.getMobileUuid());
                entity.setTokenFcm(fcmToken);
                fcmRepository.insert(entity);
            } else {
                entity.setUserMobile(usersEntity.getMobileUuid());
                entity.setTokenFcm(fcmToken);
                fcmRepository.update(entity);
            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return String.format("%s update fcm token is success.", username);
    }


}
