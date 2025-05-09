package com.example.mockvoting.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.FileInputStream;
import java.io.IOException;


@Configuration
public class GcsConfig {

    @Value("${gcs.key.location}")
    private String keyPath;

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}