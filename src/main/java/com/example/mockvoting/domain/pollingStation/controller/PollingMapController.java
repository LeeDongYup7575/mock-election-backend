package com.example.mockvoting.domain.pollingStation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 위도/경도로 행정구역 정보를 조회하는 컨트롤러입니다.
 * 네이버 Reverse Geocoding API를 사용합니다.
 */
@RestController
@RequestMapping("/api/map")
public class PollingMapController {

    @Value("${naver.map.client-id}")
    private String clientId;

    @Value("${naver.map.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 역지오코딩 요청 처리
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

            // API 인증 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            // 쿼리 파라미터 구성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("coords", longitude + "," + latitude)
                    .queryParam("output", "json")
                    .queryParam("orders", "admcode");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            // API 호출 및 응답 수신
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
