package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.*;
import com.seaman.model.response.LoginResponse;
import com.seaman.model.response.RefreshTokenResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.service.AuthService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private AuthService authService;
    private AuthController controller;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        request = mock(HttpServletRequest.class);
        when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
        when(messages.getMessageDescription(AppStatus.SUCCESS_CODE, "TH")).thenReturn("success");
        controller = new AuthController(authService, messages);
    }

    @Test
    void loginDelegatesToService() {
        LoginRequest input = new LoginRequest();
        LoginResponse data = new LoginResponse();
        when(authService.login(input)).thenReturn(data);

        ResponseEntity<SuccessResponse<LoginResponse>> response = controller.login(request, input);

        assertSuccess(response.getBody(), data);
        verify(authService).login(input);
    }

    @Test
    void registerDelegatesToService() {
        RegisterRequest input = new RegisterRequest();
        RegisterResponse data = new RegisterResponse();
        when(authService.register(input)).thenReturn(data);

        ResponseEntity<SuccessResponse<RegisterResponse>> response = controller.register(request, input);

        assertSuccess(response.getBody(), data);
        verify(authService).register(input);
    }

    @Test
    void changePasswordDelegatesToService() {
        ChangePasswordRequest input = new ChangePasswordRequest();
        RegisterResponse data = new RegisterResponse();
        when(authService.changePassword(request, input)).thenReturn(data);

        ResponseEntity<SuccessResponse<RegisterResponse>> response = controller.changePassword(request, input);

        assertSuccess(response.getBody(), data);
        verify(authService).changePassword(request, input);
    }

    @Test
    void refreshTokenDelegatesToService() {
        RefreshTokenResponse data = new RefreshTokenResponse();
        when(authService.refreshToken("refresh-token")).thenReturn(data);

        ResponseEntity<SuccessResponse<RefreshTokenResponse>> response =
                controller.refreshToken(request, "refresh-token");

        assertSuccess(response.getBody(), data);
        verify(authService).refreshToken("refresh-token");
    }

    @Test
    void activateUserDelegatesToService() {
        when(authService.activateUser(request, "uid-1")).thenReturn("activated");

        ResponseEntity<SuccessResponse<String>> response = controller.activateUser(request, "uid-1");

        assertSuccess(response.getBody(), "activated");
        verify(authService).activateUser(request, "uid-1");
    }

    @Test
    void resetPasswordDelegatesToService() {
        ResetPasswordRequest input = new ResetPasswordRequest();
        when(authService.resetPassword(request, input)).thenReturn("reset");

        ResponseEntity<SuccessResponse<String>> response = controller.resetPassword(request, input);

        assertSuccess(response.getBody(), "reset");
        verify(authService).resetPassword(request, input);
    }

    @Test
    void forgotPasswordDelegatesToService() {
        ForgotPasswordRequest input = new ForgotPasswordRequest();
        RegisterResponse data = new RegisterResponse();
        when(authService.forgotPassword(request, input)).thenReturn(data);

        ResponseEntity<SuccessResponse<RegisterResponse>> response = controller.forgotPassword(request, input);

        assertSuccess(response.getBody(), data);
        verify(authService).forgotPassword(request, input);
    }

    private <T> void assertSuccess(SuccessResponse<T> body, T data) {
        assertEquals(AppStatus.SUCCESS_CODE, body.getCode());
        assertEquals("success", body.getDescription());
        assertSame(data, body.getData());
    }
}
