package com.seaman.utils;

import com.seaman.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Base64FileValidatorTest {

    private Base64FileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new Base64FileValidator();
        ReflectionTestUtils.setField(validator, "maxDecodedBytes", 10L);
    }

    @Test
    void acceptsPngDataUriForImage() {
        validator.validateImage(dataUri("image/png", new byte[]{
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'
        }), "imageProfile");
    }

    @Test
    void rejectsPdfForImage() {
        assertThrows(BusinessException.class, () -> validator.validateImage(
                dataUri("application/pdf", new byte[]{'%', 'P', 'D', 'F', '-'}), "imageProfile"));
    }

    @Test
    void rejectsMismatchedDeclaredType() {
        assertThrows(BusinessException.class, () -> validator.validateDocument(
                dataUri("image/png", new byte[]{'%', 'P', 'D', 'F', '-'}), "fileCert"));
    }

    @Test
    void rejectsOversizedDecodedPayload() {
        assertThrows(BusinessException.class, () -> validator.validateDocument(
                dataUri("image/png", new byte[]{
                        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3
                }), "fileCert"));
    }

    private String dataUri(String contentType, byte[] bytes) {
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
