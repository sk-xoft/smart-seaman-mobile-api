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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Notifications", description = "การแจ้งเตือนของผู้ใช้")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "ส่ง Notification ทดสอบ", description = "ยิง push notification ทดสอบการทำงาน (สำหรับ dev เท่านั้น)")
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

    @Operation(summary = "รายการแจ้งเตือน", description = "ดึงรายการแจ้งเตือนทั้งหมดของผู้ใช้ รวมถึงแจ้งเตือนใบรับรองใกล้หมดอายุ")
    @GetMapping(Routes.NOTIFICATIONS)
    public ResponseEntity<SuccessResponse<NotificationResponse>> notifications(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.notifications(httpServletRequest)
        ).build());
    }

    @Operation(summary = "อัพเดตสถานะแจ้งเตือน", description = "อัพเดตสถานะอ่านแล้วของหลายแจ้งเตือนพร้อมกัน")
    @PostMapping(Routes.NOTIFICATIONS_UPDATE)
    public ResponseEntity<SuccessResponse<UpdateNotificationsResponse>> updateNotifications(HttpServletRequest httpServletRequest, @Valid @RequestBody MSendNotificationsRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.updateNotifications(httpServletRequest, request)
        ).build());
    }

    @Operation(summary = "อัพเดตแจ้งเตือนตาม ID", description = "อัพเดตสถานะอ่านแล้วของแจ้งเตือนรายการเดียว")
    @PostMapping(Routes.NOTIFICATIONS_UPDATE_VALUE_ID)
    public ResponseEntity<SuccessResponse<UpdateNotificationsResponse>> updateNotificationsByValueId(HttpServletRequest httpServletRequest, @Valid @RequestBody SendNotificationRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                sendNotificationService.updateNotificationsByValueId(httpServletRequest, request)
        ).build());
    }

    @Operation(summary = "อ่านทั้งหมด", description = "ทำเครื่องหมายแจ้งเตือนทั้งหมดว่าอ่านแล้ว")
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
