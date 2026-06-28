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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Authentication", description = "ลงทะเบียน, เข้าสู่ระบบ, และจัดการ token")
@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;
    private final MessageCodeService messageCodeService;

    @Operation(summary = "เข้าสู่ระบบ", description = "ล็อกอินด้วย email และ password รับ JWT token กลับ")
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

    @Operation(summary = "ลงทะเบียนผู้ใช้ใหม่", description = "สมัครบัญชีใหม่ ระบบจะส่งอีเมลยืนยันให้")
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

    @Operation(summary = "เปลี่ยนรหัสผ่าน", description = "เปลี่ยนรหัสผ่านของผู้ใช้ที่ login อยู่")
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

    @Operation(summary = "ต่ออายุ JWT token", description = "ใช้ refresh token เพื่อขอ access token ใหม่")
    @GetMapping(Routes.REFRESH_TOKEN)
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(HttpServletRequest httpServletRequest,
            @Parameter(description = "Refresh token", required = true) @RequestParam("refToken") String refToken) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.refreshToken(refToken)
        ).build());
    }

    @Operation(summary = "ยืนยันบัญชีผู้ใช้", description = "ยืนยันบัญชีผ่านลิงก์จากอีเมล")
    @GetMapping(Routes.ACTIVATE_USER)
    public ResponseEntity<SuccessResponse<String>> activateUser(HttpServletRequest httpServletRequest,
            @Parameter(description = "UID ยืนยันตัวตนจากอีเมล", required = true) @RequestParam("uid") String uid) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.activateUser(httpServletRequest, uid)

        ).build());
    }

    @Operation(summary = "รีเซ็ตรหัสผ่าน", description = "ตั้งรหัสผ่านใหม่ด้วย token จากอีเมล")
    @PostMapping(Routes.RESET_PASSWORD)
    public ResponseEntity<SuccessResponse<String>> resetPassword(HttpServletRequest httpServletRequest, @Valid @RequestBody ResetPasswordRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.resetPassword(httpServletRequest, request)
        ).build());
    }

    @Operation(summary = "ขอรีเซ็ตรหัสผ่าน", description = "ส่งอีเมลพร้อมลิงก์รีเซ็ตรหัสผ่านไปยัง email ที่ระบุ")
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