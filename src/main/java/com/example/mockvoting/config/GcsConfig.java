package com.example.mockvoting.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


@Configuration
public class GcsConfig {
    @Value("${gcs.key.location}")
    private String keyPath;

    @Bean
    public Storage storage() throws IOException {
        try {
            ClassPathResource path = new ClassPathResource(keyPath);
            System.out.println(path.exists() + " 찍힘?");
            GoogleCredentials credentials = GoogleCredentials.fromStream(path.getInputStream());
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("==============여기임==================");
            throw new RuntimeException(e);
        }
    }
}