package com.example.mockvoting.domain.pollingStation.controller;

import com.example.mockvoting.domain.pollingStation.service.PollingStationProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

//import java.awt.List;
import java.util.List;



// 공공데이터 API를 통해 투표소 및 사전투표소 정보를 조회하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/polling")
@Slf4j
public class PollingStationProxyController {

    private final PollingStationProxyService pollingStationProxyService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.map.client-id}")
    private String clientId;

    @Value("${naver.map.client-secret}")
    private String clientSecret;

    @Autowired
    public PollingStationProxyController(PollingStationProxyService pollingStationProxyService) {
        this.pollingStationProxyService = pollingStationProxyService;
    }

    // 투표소 정보 응답
    @GetMapping("/getPolplcOtlnmapTrnsportInfoInqire")
    public ResponseEntity<Object> getPollingStations(
            @RequestParam Map<String, String> params) {
        return pollingStationProxyService.getPollingStationsData(params);
    }

    // 사전 투표소 정보 응답
    @GetMapping("/getPrePolplcOtlnmapTrnsportInfoInqire")
    public ResponseEntity<Object> getPrePollingStations(
            @RequestParam Map<String, String> params) {
        return pollingStationProxyService.getPrePollingStationsData(params);
    }

    // 역지오코딩 요청 처리
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double latitude, @RequestParam double longitude) {
        log.info("역지오코딩 요청 - 좌표: {}, {}", latitude, longitude);

        try {
            // 네이버 역지오코딩 API URL (Naver Cloud Platform)
//            String url = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc";
            String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

            // API 인증 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            // 실제 키 값 로깅 (문제 해결 후 제거 예정)
            log.debug("Client ID: {}, Client Secret: {}", clientId, clientSecret);

            // 쿼리 파라미터 구성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("coords", longitude + "," + latitude)
                    .queryParam("output", "json")
                    .queryParam("orders", "admcode,legalcode");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.debug("API 요청 URL: {}", builder.toUriString());

            // API 호출 및 응답 수신
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class);

            log.info("네이버 API 응답 상태: {}", response.getStatusCode());

            // 네이버 API 응답에서 필요한 데이터 추출
            Map<String, Object> responseBody = response.getBody();
            Map<String, String> result = new HashMap<>();


            // 응답에서 시도명과 구군명 추출
            if (responseBody != null && responseBody.containsKey("results")) {
                // results가 List<Map<String, Object>> 형식인지 확인
                Object resultsObject = responseBody.get("results");
                if (resultsObject instanceof List) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) resultsObject;

                    if (results != null && !results.isEmpty()) {
                        for (Map<String, Object> item : results) {
                            // name이 "legalcode" 또는 "admcode"인 항목을 찾고, 지역 정보 추출
                            String name = (String) item.get("name");
                            if ("legalcode".equals(name) || "admcode".equals(name)) {
                                Map<String, Object> region = (Map<String, Object>) item.get("region");
                                if (region != null) {
                                    Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                                    Map<String, Object> area2 = (Map<String, Object>) region.get("area2");

                                    // null 체크 후 값을 추가
                                    if (area1 != null) {
                                        result.put("sdName", (String) area1.get("name"));
                                    }
                                    if (area2 != null) {
                                        result.put("wiwName", (String) area2.get("name"));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    log.error("API 응답에서 results 필드의 형식이 예상과 다릅니다.");
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("역지오코딩 요청 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage(), "errorType", e.getClass().getName()));
        }
    }

}
