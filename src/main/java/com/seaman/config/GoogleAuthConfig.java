package com.seaman.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
@ConditionalOnProperty(prefix = "fcm.firebase", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoogleAuthConfig {

    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/firebase.messaging"
    );

    @Value("${fcm.firebase.credential.file}")
    private String authFirebaseFileName;

    @Bean
    GoogleCredentials credentialFromFile() throws IOException {
        if (!StringUtils.hasText(authFirebaseFileName)) {
            throw new IOException("fcm.firebase.credential.file is required when FCM is enabled");
        }
        Resource serviceAccount = authFirebaseFileName.startsWith("/")
                ? new FileSystemResource(authFirebaseFileName)
                : new ClassPathResource(authFirebaseFileName);
        if (!serviceAccount.exists() || !serviceAccount.isReadable()) {
            throw new IOException("Firebase credential file is not readable: " + authFirebaseFileName);
        }
        return GoogleCredentials.fromStream(serviceAccount.getInputStream())
                .createScoped(SCOPES);
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}
