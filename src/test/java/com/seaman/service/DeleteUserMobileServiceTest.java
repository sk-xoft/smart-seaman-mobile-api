package com.seaman.service;

import com.seaman.entity.CertificateEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserMobileServiceTest {

    @Mock UserRepository userRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock EmailService emailService;

    private DeleteUserMobileService service;

    @BeforeEach
    void setUp() {
        service = new DeleteUserMobileService(userRepository, certificateRepository, emailService);
        ReflectionTestUtils.setField(service, "emailTo", "admin@example.com");
    }

    @Test
    void deleteUserIsOverDueDateContinuesLoopAfterOneFailureAndAlwaysSendsEmail() {
        UsersEntity success = userEntity("u1", "success@example.com");
        UsersEntity failure = userEntity("u2", "failure@example.com");
        UsersEntity throwing = userEntity("u3", "throwing@example.com");
        when(userRepository.getUserIsDeleteOverDueDate()).thenReturn(List.of(success, failure, throwing));
        when(userRepository.deleteUserIsStatusDeleteOverDueDate("u1")).thenReturn(true);
        when(userRepository.deleteUserIsStatusDeleteOverDueDate("u2")).thenReturn(false);
        when(userRepository.deleteUserIsStatusDeleteOverDueDate("u3")).thenThrow(new RuntimeException("db down"));

        CertificateEntity certSuccess = certEntity("c1", "u1", "DOC001");
        CertificateEntity certThrowing = certEntity("c2", "u2", "DOC002");
        when(certificateRepository.getCertificationIsNotUserMobileOwner())
                .thenReturn(List.of(certSuccess, certThrowing));
        when(certificateRepository.delete("c1")).thenReturn(true);
        when(certificateRepository.delete("c2")).thenThrow(new RuntimeException("cert db down"));

        service.deleteUserIsOverDueDate();

        ArgumentCaptor<Map<String, String>> userResultCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, String>> certResultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmailDeleteUser(eq("admin@example.com"),
                userResultCaptor.capture(), certResultCaptor.capture());

        Map<String, String> userResult = userResultCaptor.getValue();
        assertEquals(3, userResult.size());
        assertTrue(userResult.get("1").contains("SUCCESS"));
        assertTrue(userResult.get("2").contains("FAILED"));
        assertTrue(userResult.get("3").contains("db down"));

        Map<String, String> certResult = certResultCaptor.getValue();
        assertEquals(2, certResult.size());
        assertTrue(certResult.get("1").contains("SUCCESS"));
        assertTrue(certResult.get("2").contains("cert db down"));

        // Both repository loops ran to completion despite mid-loop failures.
        verify(userRepository, times(3)).deleteUserIsStatusDeleteOverDueDate(anyString());
        verify(certificateRepository, times(2)).delete(anyString());
    }

    @Test
    void deleteUserIsOverDueDateSendsEmailWithEmptyMapsWhenNothingToDelete() {
        when(userRepository.getUserIsDeleteOverDueDate()).thenReturn(Collections.emptyList());
        when(certificateRepository.getCertificationIsNotUserMobileOwner()).thenReturn(Collections.emptyList());

        service.deleteUserIsOverDueDate();

        verify(emailService).sendEmailDeleteUser(eq("admin@example.com"), eq(Collections.emptyMap()),
                eq(Collections.emptyMap()));
    }

    private UsersEntity userEntity(String mobileUuid, String email) {
        UsersEntity entity = new UsersEntity();
        entity.setMobileUuid(mobileUuid);
        entity.setMobileUserId("1");
        entity.setSmartSeamanId("00001");
        entity.setEmail(email);
        return entity;
    }

    private CertificateEntity certEntity(String certId, String mobileUuid, String documentCode) {
        CertificateEntity entity = new CertificateEntity();
        entity.setCertId(certId);
        entity.setCertMobileUuid(mobileUuid);
        entity.setCertDocumentCode(documentCode);
        return entity;
    }
}
