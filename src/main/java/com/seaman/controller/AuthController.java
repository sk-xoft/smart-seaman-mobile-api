package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.*;
import com.seaman.model.response.LoginResponse;
import com.seaman.model.response.RefreshTokenResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.service.AuthService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;
    private final MessageCodeService messageCodeService;

    @PostMapping(Routes.LOGIN)
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody LoginRequest loginRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.login(loginRequest)
        ).build());
    }

    @PostMapping(Routes.REGISTER)
    public ResponseEntity<SuccessResponse<RegisterResponse>> register(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody RegisterRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.register(request)
        ).build());
    }

    @PostMapping(Routes.CHANGE_PASSWORD)
    public ResponseEntity<SuccessResponse<RegisterResponse>> changePassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ChangePasswordRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.changePassword(httpServletRequest, request)
        ).build());
    }

    @GetMapping(Routes.REFRESH_TOKEN)
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(HttpServletRequest httpServletRequest, @RequestParam("refToken") String refToken) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.refreshToken(refToken)
        ).build());
    }

    @GetMapping(Routes.ACTIVATE_USER)
    public ResponseEntity<SuccessResponse<String>> activateUser(HttpServletRequest httpServletRequest,  @RequestParam("uid") String uid) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.activateUser(httpServletRequest, uid)

        ).build());
    }

    @PostMapping(Routes.RESET_PASSWORD)
    public ResponseEntity<SuccessResponse<String>> resetPassword(HttpServletRequest httpServletRequest, @Valid @RequestBody ResetPasswordRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.resetPassword(httpServletRequest, request)
        ).build());
    }

    @PostMapping(Routes.FORGOT_PASSWORD)
    public ResponseEntity<SuccessResponse<RegisterResponse>> forgotPassword(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ForgotPasswordRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.forgotPassword(httpServletRequest, request)
        ).build());
    }
}