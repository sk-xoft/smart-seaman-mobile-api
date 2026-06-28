package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.FcmNotificationRequest;
import com.seaman.model.response.FormResponse;
import com.seaman.service.FcmNotificationService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "FCM", description = "Firebase Cloud Messaging — จัดการ token สำหรับ Push Notification")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class FcmController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final FcmNotificationService fcmNotificationService;

    @Operation(summary = "อัพเดต FCM Token", description = "บันทึก FCM token ของอุปกรณ์เพื่อรับ push notification")
    @PostMapping(Routes.FCM_UPDATE)
    public ResponseEntity<SuccessResponse<FormResponse>> listSchoolTraining(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FcmNotificationRequest request
            ) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                fcmNotificationService.fcmUpdate(httpServletRequest, request.getTokenFcm())
        ).build());
    }

}
