package com.example.mockvoting.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcsConfig {

    @Bean
    public Storage storage() throws IOException {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        GoogleCredentials credentials;
        if (keyPath != null && !keyPath.isBlank()) {
            credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath)); // ✅ GCP Cloud Run용
        } else {
            // fallback: 로컬 개발용 (resources 아래 경로)
            credentials = GoogleCredentials.fromStream(
                    new ClassPathResource("gcs-key.json").getInputStream()
            );
        }

        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

}
