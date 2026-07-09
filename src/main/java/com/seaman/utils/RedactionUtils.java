package com.seaman.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RedactionUtils {

    private static final Gson GSON = new Gson();
    private static final String MASKED = "XXXXXXX";
    private static final String FILE_MASKED = "FileBase64";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "authorization", "token", "accessToken", "refreshToken", "jwt", "jwtSecret",
            "password", "oldPassword", "confirmPassword", "newPassword",
            "email", "mail", "mobileNumber", "mobileNo", "phone", "address",
            "dbPassword", "secret", "secretKey", "objectStoreKey", "objectStoreSecret",
            "omiseSecretKey", "omiseWebhookSecret"
    );
    private static final Set<String> FILE_FIELDS = Set.of(
            "file", "fileCert", "imageProfile", "fileBase64", "base64", "image",
            "bannerImage", "newsImage", "voucherImage", "qrImage"
    );

    private RedactionUtils() {
    }

    public static String redactJsonObject(Object body) {
        if (body == null) {
            return null;
        }
        return redactJsonString(GSON.toJson(body));
    }

    public static String redactJsonString(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            redact(element);
            return GSON.toJson(element);
        } catch (Exception ex) {
            return json;
        }
    }

    public static Map<String, String> redactMap(Map<String, String> values) {
        values.replaceAll((key, value) -> isSensitiveField(key) ? MASKED : value);
        return values;
    }

    private static void redact(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : Set.copyOf(object.keySet())) {
                if (isFileField(key)) {
                    object.addProperty(key, FILE_MASKED);
                } else if (isSensitiveField(key)) {
                    object.addProperty(key, MASKED);
                } else {
                    redact(object.get(key));
                }
            }
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(RedactionUtils::redact);
        }
    }

    private static boolean isSensitiveField(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELDS.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> normalized.equals(value)
                        || normalized.endsWith(value)
                        || normalized.contains("password")
                        || normalized.contains("token")
                        || normalized.contains("secret"));
    }

    private static boolean isFileField(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return FILE_FIELDS.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> normalized.equals(value) || normalized.endsWith(value));
    }
}
