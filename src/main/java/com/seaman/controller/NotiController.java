package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.MSendNotificationsRequest;
import com.seaman.model.request.SendNotificationRequest;
import com.seaman.model.response.NotificationResponse;
import com.seaman.model.response.UpdateNotificationsResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.SendNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class NotiController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final SendNotificationService sendNotificationService;


    /**
     * เส้นนี้สำหรับ ยิ่ง noti เอง เพื่อให้ทดสอบการทำงาน noti ว่าทำงานได้หรือเปล่าไหม ?
     * @param httpServletRequest
     * @return
     */
    @GetMapping(Routes.NOTI_MANUAL)
    public ResponseEntity<SuccessResponse<String>> sendNotiManual(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        sendNotificationService.sendNotification();

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                "success"
        ).build());
    }

    @GetMapping(Routes.NOTIFICATIONS)
    public ResponseEntity<SuccessResponse<NotificationResponse>> notifications(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.notifications(httpServletRequest)
        ).build());
    }

    @PostMapping(Routes.NOTIFICATIONS_UPDATE)
    public ResponseEntity<SuccessResponse<UpdateNotificationsResponse>> updateNotifications(HttpServletRequest httpServletRequest, @Valid @RequestBody MSendNotificationsRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.updateNotifications(httpServletRequest, request)
        ).build());
    }

    @PostMapping(Routes.NOTIFICATIONS_UPDATE_VALUE_ID)
    public ResponseEntity<SuccessResponse<UpdateNotificationsResponse>> updateNotificationsByValueId(HttpServletRequest httpServletRequest, @Valid @RequestBody SendNotificationRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.updateNotificationsByValueId(httpServletRequest, request)
        ).build());
    }

    @GetMapping(Routes.NOTIFICATIONS_UPDATE_ALL)
    public ResponseEntity<SuccessResponse<UpdateNotificationsResponse>> updateNotifications(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.updateAllNotifications(httpServletRequest)
        ).build());
    }

}
