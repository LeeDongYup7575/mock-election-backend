package com.example.mockvoting.domain.user.service;

import com.example.mockvoting.exception.CustomException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    /**
     * 구글 ID 토큰 검증 및 사용자 정보 추출
     */
    public Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new CustomException("유효하지 않은 구글 토큰입니다.");
            }

            Payload payload = token.getPayload();

            // 구글 계정 정보
            String sub = payload.getSubject(); // 사용자의 Google ID
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            if (!emailVerified) {
                throw new CustomException("이메일 인증이 완료되지 않은 구글 계정입니다.");
            }

            // 사용자 정보를 맵으로 반환
            return Map.of(
                    "sub", sub,  // 구글에서 제공하는 고유 ID (userId로 사용)
                    "email", email,
                    "name", name,
                    "pictureUrl", pictureUrl
            );
        } catch (GeneralSecurityException | IOException e) {
            log.error("구글 토큰 검증 중 오류 발생: {}", e.getMessage());
            throw new CustomException("구글 인증 처리 중 오류가 발생했습니다.");
        }
    }
}
