package com.example.mockvoting.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcsConfig {

    @Value("${gcs.key.location}")
    private String keyPath;

    @Bean
    public Storage storage() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(keyPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}
