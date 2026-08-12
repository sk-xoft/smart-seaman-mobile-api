package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenService is pure logic over a real SecretKey, so no mocks are needed for the crypto
 * itself — only a real key and (where required) a mocked UserDetails.
 */
class JwtTokenServiceTest {

    private JwtTokenService service;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        service = new JwtTokenService(secretKey);
    }

    @Test
    void generateAndParseTokenRoundTrips() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", "session-id-1");

        String token = service.generateToken(claims, "crew@example.com");

        assertEquals("crew@example.com", service.getUsernameFromToken(token));
        assertEquals("session-id-1", service.getJti(token));
        assertTrue(service.getExpirationDateFromToken(token).after(new Date()));
        assertTrue(service.verifyToken(token));
    }

    @Test
    void generateTokenFromUserDetailsUsesUsername() {
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("crew@example.com");

        String token = service.generateToken(userDetails);

        assertEquals("crew@example.com", service.getUsernameFromToken(token));
    }

    @Test
    void parseClaimsThrowsBusinessExceptionForMalformedToken() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.parseClaims("not-a-valid-jwt-token"));
        assertEquals(AppStatus.JWT_SIGNATURE_INVALID, ex.getCode());
    }

    @Test
    void parseClaimsThrowsBusinessExceptionForWrongSigningKey() {
        String token = service.generateToken(new HashMap<>(), "crew@example.com");
        SecretKey otherKey = new SecretKeySpec(new byte[32], "HmacSHA256");
        JwtTokenService otherService = new JwtTokenService(otherKey);

        BusinessException ex = assertThrows(BusinessException.class, () -> otherService.parseClaims(token));
        assertEquals(AppStatus.JWT_SIGNATURE_INVALID, ex.getCode());
    }

    @Test
    void parseClaimsThrowsBusinessExceptionForExpiredToken() {
        String expiredToken = Jwts.builder()
                .setSubject("crew@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60_000))
                .setExpiration(new Date(System.currentTimeMillis() - 30_000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.parseClaims(expiredToken));
        assertEquals(AppStatus.JWT_EXPIRE, ex.getCode());
    }

    @Test
    void validateTokenWithUserDetailsSucceedsForMatchingUsername() {
        String token = service.generateToken(new HashMap<>(), "crew@example.com");
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("crew@example.com");

        assertTrue(service.validateToken(token, userDetails));
    }

    @Test
    void validateTokenWithUserDetailsFailsForMismatchedUsername() {
        String token = service.generateToken(new HashMap<>(), "crew@example.com");
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("other@example.com");

        assertFalse(service.validateToken(token, userDetails));
    }

    @Test
    void validateTokenWithSubjectSucceedsForMatchingSubject() {
        String token = service.generateToken(new HashMap<>(), "crew@example.com");

        assertTrue(service.validateToken(token, "crew@example.com"));
        assertFalse(service.validateToken(token, "other@example.com"));
    }

    @Test
    void validateTokenReturnsTrueForNonExpiredToken() {
        String token = service.generateToken(new HashMap<>(), "crew@example.com");

        assertTrue(service.validateToken(token));
    }
}
