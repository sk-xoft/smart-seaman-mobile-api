package com.seaman.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender javaMailSender;

    private EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService(javaMailSender);
    }

    // ---- sendEmailForgotPassword

    @Test
    void sendEmailForgotPasswordReturnsSuccessWhenMailSent() {
        when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage());

        String result = service.sendEmailForgotPassword("Crew Member", "crew@example.com",
                "https://app/confirm-forgot", "mobile-uuid");

        assertEquals("success", result);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailForgotPasswordReturnsSuccessEvenWhenSendingFails() {
        // sendEmail() swallows any exception internally and only returns a status string that is
        // logged, not propagated — the public method always reports "success" regardless.
        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));

        String result = service.sendEmailForgotPassword("Crew Member", "crew@example.com",
                "https://app/confirm-forgot", "mobile-uuid");

        assertEquals("success", result);
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    // ---- sendEmailRegister

    @Test
    void sendEmailRegisterReturnsSuccessWhenMailSent() {
        when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage());

        String result = service.sendEmailRegister("Crew Member", "crew@example.com",
                "https://app/confirm-register", "mobile-uuid");

        assertEquals("success", result);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailRegisterReturnsSuccessEvenWhenSendingFails() {
        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));

        String result = service.sendEmailRegister("Crew Member", "crew@example.com",
                "https://app/confirm-register", "mobile-uuid");

        assertEquals("success", result);
    }

    // ---- sendEmailDeleteUser

    @Test
    void sendEmailDeleteUserReturnsSuccessWhenMailSent() {
        when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage());
        Map<String, String> resultUser = new HashMap<>();
        resultUser.put("u1", "user1 deleted");
        Map<String, String> resultCert = new HashMap<>();
        resultCert.put("c1", "cert1 deleted");

        String result = service.sendEmailDeleteUser("admin@example.com", resultUser, resultCert);

        assertEquals("success", result);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailDeleteUserReturnsSuccessWithEmptyMaps() {
        when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage());

        String result = service.sendEmailDeleteUser("admin@example.com", new HashMap<>(), new HashMap<>());

        assertEquals("success", result);
    }

    private MimeMessage realMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }
}
