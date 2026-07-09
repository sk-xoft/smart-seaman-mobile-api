package com.seaman.utils;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Set;

@Component
public class Base64FileValidator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg");
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("image/png", "image/jpeg", "application/pdf");

    @Value("${smart-seaman.file-upload.max-base64-bytes:10485760}")
    private long maxDecodedBytes;

    public void validateImage(String value, String fieldName) {
        validate(value, fieldName, ALLOWED_IMAGE_TYPES);
    }

    public void validateDocument(String value, String fieldName) {
        validate(value, fieldName, ALLOWED_DOCUMENT_TYPES);
    }

    public boolean hasContent(String value) {
        return StringUtils.hasText(value);
    }

    private void validate(String value, String fieldName, Set<String> allowedContentTypes) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, fieldName);
        }
        ParsedBase64 parsed = parse(value, fieldName);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(parsed.base64);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, fieldName);
        }
        if (decoded.length == 0 || decoded.length > maxDecodedBytes) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, fieldName);
        }
        String detectedType = detectContentType(decoded);
        String declaredType = parsed.contentType;
        String contentType = StringUtils.hasText(declaredType) ? declaredType : detectedType;
        if (!allowedContentTypes.contains(contentType) || !contentType.equals(detectedType)) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, fieldName);
        }
    }

    private ParsedBase64 parse(String value, String fieldName) {
        String trimmed = value.trim();
        if (trimmed.startsWith("data:")) {
            int commaIndex = trimmed.indexOf(',');
            int base64MarkerIndex = trimmed.indexOf(";base64");
            if (commaIndex < 0 || base64MarkerIndex < 0 || base64MarkerIndex > commaIndex) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, fieldName);
            }
            return new ParsedBase64(trimmed.substring(5, base64MarkerIndex), trimmed.substring(commaIndex + 1));
        }
        return new ParsedBase64(null, trimmed);
    }

    private String detectContentType(byte[] bytes) {
        if (startsWith(bytes, new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})) {
            return "image/png";
        }
        if (startsWith(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        if (startsWith(bytes, new byte[]{'%', 'P', 'D', 'F', '-'})) {
            return "application/pdf";
        }
        throw new BusinessException(AppStatus.INVALID_FORMAT, "fileContent");
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static class ParsedBase64 {
        private final String contentType;
        private final String base64;

        private ParsedBase64(String contentType, String base64) {
            this.contentType = contentType;
            this.base64 = base64;
        }
    }
}
