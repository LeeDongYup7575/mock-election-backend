package com.example.mockvoting.domain.spamcheck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {
    @Value("${recaptcha.secret}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 사용자가 제출한 캡챠 토큰을 구글 서버에 검증 요청하고,
     * 검증 결과가 성공인지 여부를 반환
     */
    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        // POST form-data 구성
        MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("secret", secretKey);
        requestData.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<RecaptchaResponse> response = restTemplate.postForEntity(
                    verifyUrl, entity, RecaptchaResponse.class
            );

            boolean success = response.getBody() != null && response.getBody().success();
            if (!success) {
                log.warn("캡챠 인증 실패: {}", response.getBody());
            }
            log.info("캡챠 인증 성공");
            return success;
        } catch (Exception e) {
            log.error("캡챠 인증 요청 실패", e);
            return false;
        }
    }

    // 내부 응답 매핑 클래스
    private record RecaptchaResponse(boolean success, String challenge_ts, String hostname, float score, String action) {}
}
