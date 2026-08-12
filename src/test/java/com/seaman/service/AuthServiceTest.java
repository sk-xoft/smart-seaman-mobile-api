package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.SessionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.ChangePasswordRequest;
import com.seaman.model.request.ForgotPasswordRequest;
import com.seaman.model.request.LoginRequest;
import com.seaman.model.request.RegisterRequest;
import com.seaman.model.request.ResetPasswordRequest;
import com.seaman.model.response.LoginResponse;
import com.seaman.model.response.RefreshTokenResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.ForgotPasswordRepository;
import com.seaman.repository.SessionRepository;
import com.seaman.repository.UserRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock HttpServletRequest httpServletRequest;
    @Mock JwtTokenService jwtTokenUtil;
    @Mock FrameworkUtils frameworkUtils;
    @Mock UserRepository userRepository;
    @Mock SessionRepository sessionRepository;
    @Mock DocumentRepository documentRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock TransactionLogsService tsnLogSrv;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ForgotPasswordRepository forgotPasswordRepository;
    @Mock DateUtil dateUtil;
    @Mock EmailService emailService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(httpServletRequest, jwtTokenUtil, frameworkUtils, userRepository,
                sessionRepository, documentRepository, certificateRepository, tsnLogSrv, passwordEncoder,
                forgotPasswordRepository, dateUtil, emailService);
        ReflectionTestUtils.setField(service, "linkConfirmRegister", "https://app/confirm-register");
        ReflectionTestUtils.setField(service, "linkConfirmForgot", "https://app/confirm-forgot");

        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- login

    @Test
    void loginThrowsWhenUsernameNotFound() {
        LoginRequest req = loginRequest();
        when(userRepository.findByUsername("crew@example.com")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(req));
        assertEquals(AppStatus.EXCEPTION_USERNAME_INCORRECT, ex.getCode());
        verify(tsnLogSrv).insert(any(), any(), eq("LOGIN"), eq("crew@example.com"));
        verify(tsnLogSrv).update(any(), any(), eq(AppStatus.EXCEPTION_USERNAME_INCORRECT), eq(""));
    }

    @Test
    void loginThrowsWhenPasswordMismatch() {
        LoginRequest req = loginRequest();
        UsersEntity user = activeUser();
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(req));
        assertEquals(AppStatus.EXCEPTION_USER_HERO_INCORRECT, ex.getCode());
    }

    @Test
    void loginThrowsWhenUserInactivated() {
        LoginRequest req = loginRequest();
        UsersEntity user = activeUser();
        user.setUserStatus("D");
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(req));
        assertEquals(AppStatus.USER_IS_INACTIVATED, ex.getCode());
    }

    @Test
    void loginThrowsWhenUserStatusNotActive() {
        LoginRequest req = loginRequest();
        UsersEntity user = activeUser();
        user.setUserStatus("I");
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(req));
        assertEquals(AppStatus.SECURITY_NOT_FOUND_USERNAME, ex.getCode());
    }

    @Test
    void loginReturnsTokenAndStoresSessionOnSuccess() {
        LoginRequest req = loginRequest();
        UsersEntity user = activeUser();
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);
        when(frameworkUtils.generateUUID()).thenReturn("session-uuid");
        when(jwtTokenUtil.generateToken(any(), eq("crew@example.com"))).thenReturn("jwt-token");
        when(dateUtil.convertTime(anyLong())).thenReturn("2026-08-08 10:00:00");

        LoginResponse response = service.login(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals("session-uuid", response.getRefToken());
        assertEquals("crew@example.com", response.getUsername());
        assertEquals("2026-08-08 10:00:00", response.getLastLoginDateTime());
        verify(sessionRepository).insert(any(SessionEntity.class));
        verify(sessionRepository).updateStatus(any(SessionEntity.class));
        verify(httpServletRequest).setAttribute(eq("sessionObject"), any(SessionEntity.class));
        verify(tsnLogSrv).update(any(), any(), eq(AppStatus.SUCCESS_CODE), eq("crew@example.com"));
    }

    // ---- register

    @Test
    void registerThrowsWhenEmailAlreadyRegistered() {
        RegisterRequest req = registerRequest();
        when(userRepository.countMax()).thenReturn(1);
        when(userRepository.findByEmail("crew@example.com")).thenReturn(activeUser());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.register(req));
        assertEquals(AppStatus.EMAIL_IS_REGISTER, ex.getCode());
        verify(userRepository, never()).insert(any());
    }

    @Test
    void registerThrowsWhenInsertFails() {
        RegisterRequest req = registerRequest();
        when(userRepository.countMax()).thenReturn(1);
        when(userRepository.findByEmail("crew@example.com")).thenReturn(null);
        when(frameworkUtils.generateUUID()).thenReturn("mobile-uuid");
        when(frameworkUtils.padLeftZeros(anyString(), eq(5))).thenReturn("00001");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.insert(any(UsersEntity.class))).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.register(req));
        assertEquals(AppStatus.CANNOT_REGISTER, ex.getCode());
        verify(documentRepository, never()).findDefault();
        verify(emailService, never()).sendEmailRegister(any(), any(), any(), any());
    }

    @Test
    void registerCreatesDefaultCertificatesAndSendsEmailOnSuccess() {
        RegisterRequest req = registerRequest();
        when(userRepository.countMax()).thenReturn(1);
        when(userRepository.findByEmail("crew@example.com")).thenReturn(null);
        when(frameworkUtils.generateUUID()).thenReturn("mobile-uuid");
        when(frameworkUtils.padLeftZeros(anyString(), eq(5))).thenReturn("00001");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.insert(any(UsersEntity.class))).thenReturn(true);

        DocumentEntity doc = new DocumentEntity();
        doc.setDocumentCode("DOC001");
        when(documentRepository.findDefault()).thenReturn(List.of(doc));

        RegisterResponse response = service.register(req);

        assertEquals("crew@example.com", response.getUsername());
        assertEquals("crew@example.com", response.getEmail());
        verify(certificateRepository).insert(any(CertificateEntity.class));
        verify(emailService).sendEmailRegister("Crew Member", "crew@example.com",
                "https://app/confirm-register", "mobile-uuid");
    }

    // ---- refreshToken

    @Test
    void refreshTokenThrowsWhenSessionNotFound() {
        when(sessionRepository.findByClientSessionId("ref-token")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.refreshToken("ref-token"));
        assertEquals(AppStatus.INVALID_UUID, ex.getCode());
    }

    @Test
    void refreshTokenGeneratesNewTokenAndUpdatesSession() {
        SessionEntity session = new SessionEntity();
        session.setClientSessionId("ref-token");
        session.setUserId("mobile-uuid");
        when(sessionRepository.findByClientSessionId("ref-token")).thenReturn(session);
        when(userRepository.findByUserUID("mobile-uuid")).thenReturn(activeUser());
        when(jwtTokenUtil.generateToken(any(), eq("crew@example.com"))).thenReturn("new-jwt-token");

        RefreshTokenResponse response = service.refreshToken("ref-token");

        assertEquals("new-jwt-token", response.getToken());
        assertEquals("new-jwt-token", session.getToken());
        verify(sessionRepository).update(session);
        verify(httpServletRequest).setAttribute("clientSessionId", "ref-token");
    }

    // ---- changePassword

    @Test
    void changePasswordThrowsWhenConfirmDoesNotMatchNew() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new1");
        req.setConfirmPassword("new2");
        stubUserObjectAttribute(activeUser());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changePassword(httpServletRequest, req));
        assertEquals(AppStatus.HERO_IS_MATCH, ex.getCode());
    }

    @Test
    void changePasswordThrowsWhenOldPasswordIncorrect() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("wrong-old");
        req.setNewPassword("new1");
        req.setConfirmPassword("new1");
        UsersEntity user = activeUser();
        stubUserObjectAttribute(user);
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong-old", user.getPassword())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changePassword(httpServletRequest, req));
        assertEquals(AppStatus.EXCEPTION_USER_HERO_INCORRECT, ex.getCode());
    }

    @Test
    void changePasswordUpdatesPasswordOnSuccess() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new1");
        req.setConfirmPassword("new1");
        UsersEntity user = activeUser();
        stubUserObjectAttribute(user);
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.matches("old", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new1")).thenReturn("encoded-new1");

        RegisterResponse response = service.changePassword(httpServletRequest, req);

        assertEquals("crew@example.com", response.getEmail());
        verify(userRepository).changePassword(user);
        assertEquals("encoded-new1", user.getPassword());
    }

    // ---- activateUser

    @Test
    void activateUserThrowsWhenUserNotFound() {
        when(userRepository.findByUserUID("uid-1")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.activateUser(httpServletRequest, "uid-1"));
        assertEquals(AppStatus.DATA_NOT_FOUND, ex.getCode());
    }

    @Test
    void activateUserThrowsWhenAlreadyActive() {
        UsersEntity user = activeUser();
        when(userRepository.findByUserUID("uid-1")).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.activateUser(httpServletRequest, "uid-1"));
        assertEquals(AppStatus.USER_IS_ACTIVATED, ex.getCode());
    }

    @Test
    void activateUserActivatesInactiveUser() {
        UsersEntity user = activeUser();
        user.setUserStatus("I");
        when(userRepository.findByUserUID("uid-1")).thenReturn(user);

        Object response = service.activateUser(httpServletRequest, "uid-1");

        assertEquals("A", user.getUserStatus());
        verify(userRepository).updateStatus(user);
        assertEquals(RegisterResponse.class, response.getClass());
    }

    // ---- resetPassword

    @Test
    void resetPasswordThrowsWhenUserNotFound() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.resetPassword(httpServletRequest, req));
        assertEquals(AppStatus.DATA_NOT_FOUND, ex.getCode());
    }

    @Test
    void resetPasswordSendsEmailAndInsertsForgotPasswordRecord() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("crew@example.com");
        UsersEntity user = activeUser();
        when(userRepository.findByEmail("crew@example.com")).thenReturn(user);

        Object response = service.resetPassword(httpServletRequest, req);

        assertEquals(RegisterResponse.class, response.getClass());
        verify(emailService).sendEmailForgotPassword("Crew Member", "crew@example.com",
                "https://app/confirm-forgot", "mobile-user-uuid");
        verify(forgotPasswordRepository).insert("mobile-user-uuid");
    }

    // ---- forgotPassword

    @Test
    void forgotPasswordThrowsWhenUserNotFoundByUid() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setUid("uid-1");
        req.setPassword("new1");
        req.setConfirmPassword("new1");
        when(userRepository.findByUserUID("uid-1")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.forgotPassword(httpServletRequest, req));
        assertEquals(AppStatus.DATA_NOT_FOUND, ex.getCode());
    }

    @Test
    void forgotPasswordThrowsWhenPasswordsMismatch() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setUid("uid-1");
        req.setPassword("new1");
        req.setConfirmPassword("new2");
        when(userRepository.findByUserUID("uid-1")).thenReturn(activeUser());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.forgotPassword(httpServletRequest, req));
        assertEquals(AppStatus.HERO_IS_MATCH, ex.getCode());
    }

    @Test
    void forgotPasswordThrowsWhenUsernameLookupFails() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setUid("uid-1");
        req.setPassword("new1");
        req.setConfirmPassword("new1");
        when(userRepository.findByUserUID("uid-1")).thenReturn(activeUser());
        when(userRepository.findByUsername("crew@example.com")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.forgotPassword(httpServletRequest, req));
        assertEquals(AppStatus.EXCEPTION_USER_HERO_INCORRECT, ex.getCode());
    }

    @Test
    void forgotPasswordUpdatesPasswordOnSuccess() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setUid("uid-1");
        req.setPassword("new1");
        req.setConfirmPassword("new1");
        UsersEntity user = activeUser();
        when(userRepository.findByUserUID("uid-1")).thenReturn(user);
        when(userRepository.findByUsername("crew@example.com")).thenReturn(user);
        when(passwordEncoder.encode("new1")).thenReturn("encoded-new1");

        Object response = service.forgotPassword(httpServletRequest, req);

        assertEquals(RegisterResponse.class, response.getClass());
        assertEquals("encoded-new1", user.getPassword());
        verify(userRepository).changePassword(user);
    }

    // ---- fixtures

    private LoginRequest loginRequest() {
        LoginRequest req = new LoginRequest();
        req.setUsername("crew@example.com");
        req.setPassword("secret");
        return req;
    }

    private RegisterRequest registerRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Crew");
        req.setLastName("Member");
        req.setEmail("crew@example.com");
        req.setPassword("secret");
        req.setPositionCode("POS001");
        req.setCompanyCode("COMP001");
        req.setMobileNumber("0812345678");
        return req;
    }

    private UsersEntity activeUser() {
        UsersEntity user = new UsersEntity();
        user.setUsername("crew@example.com");
        user.setEmail("crew@example.com");
        user.setPassword("encoded-password");
        user.setMobileUuid("mobile-user-uuid");
        user.setFirstName("Crew");
        user.setLastName("Member");
        user.setUserStatus("A");
        return user;
    }

    private void stubUserObjectAttribute(UsersEntity user) {
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
    }
}
