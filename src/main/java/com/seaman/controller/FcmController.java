package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.FcmNotificationRequest;
import com.seaman.model.response.FormResponse;
import com.seaman.service.FcmNotificationService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class FcmController  extends BaseController {

    private final MessageCodeService messageCodeService;

    private final FcmNotificationService fcmNotificationService;

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
