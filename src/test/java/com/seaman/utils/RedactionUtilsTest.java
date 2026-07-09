package com.seaman.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedactionUtilsTest {

    @Test
    void redactsSensitiveFieldsAndBase64FilesRecursively() {
        String redacted = RedactionUtils.redactJsonString("{"
                + "\"password\":\"secret\","
                + "\"mobileNumber\":\"0812345678\","
                + "\"email\":\"user@example.com\","
                + "\"fileCert\":\"base64payload\","
                + "\"items\":[{\"token\":\"jwt-value\",\"address\":\"bangkok\"}]"
                + "}");

        assertFalse(redacted.contains("secret"));
        assertFalse(redacted.contains("0812345678"));
        assertFalse(redacted.contains("user@example.com"));
        assertFalse(redacted.contains("base64payload"));
        assertFalse(redacted.contains("jwt-value"));
        assertFalse(redacted.contains("bangkok"));
        assertTrue(redacted.contains("XXXXXXX"));
        assertTrue(redacted.contains("FileBase64"));
    }
}
