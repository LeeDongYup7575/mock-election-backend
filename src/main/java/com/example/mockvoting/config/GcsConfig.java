package com.example.mockvoting.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcsConfig {

    @Bean
    public Storage storage() throws IOException {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (keyPath == null || keyPath.isBlank()) {
            keyPath = "src/main/resources/gcs-key.json"; // fallback 경로
        }
        if (keyPath == null || keyPath.isBlank()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS 환경 변수가 설정되지 않았습니다.");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}
