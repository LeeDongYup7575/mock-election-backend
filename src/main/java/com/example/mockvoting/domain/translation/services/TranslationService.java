package com.example.mockvoting.domain.translation.services;


import com.example.mockvoting.domain.translation.dto.TranslationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;

    @Value("${google.translate.api.key}")
    private String apiKey;

    @Value("${google.translate.api.url:https://translation.googleapis.com/language/translate/v2}")
    private String apiUrl;

    public TranslationService() {
        this.restTemplate = new RestTemplate();
    }

    public TranslationResponse translateTexts(List<String> texts, String targetLanguage) {
        // API 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // API 요청 본문 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("q", texts);
        requestBody.put("target", targetLanguage);
        requestBody.put("format", "text");

        // API 요청 URL 생성 (API 키 포함)
        String requestUrl = apiUrl + "?key=" + apiKey;

        // API 요청 및 응답 처리
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, request, Map.class);

        // Google API 응답 파싱
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, String>> googleTranslations = (List<Map<String, String>>)
                ((Map<String, Object>) responseBody.get("data")).get("translations");

        // 응답 변환
        List<TranslationResponse.Translation> translations = new ArrayList<>();
        for (Map<String, String> item : googleTranslations) {
            translations.add(new TranslationResponse.Translation(item.get("translatedText")));
        }

        return new TranslationResponse(translations);
    }
}
