package com.example.mockvoting.domain.pollingStation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * 공공데이터 포털의 투표소 관련 API를 호출하여,
 * 선거일 투표소 및 사전투표소 정보를 가져오는 서비스 클래스입니다.
 *
 * RestTemplate을 사용해 외부 API에 요청하고, 결과를 반환합니다.
 */
@Service
public class PollingStationProxyService {

    // HTTP 요청을 보내기 위한 RestTemplate 인스턴스 생성
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${polling.api.base-url}")
    private String baseUrl;

    @Value("${polling.api.service-key}")
    private String serviceKey;

    // 선거일 투표소 정보를 가져오는 메서드
    public ResponseEntity<Object> getPollingStationsData(Map<String, String> params) {
        return fetchData("/9760000/PolplcInfoInqireService2/getPolplcOtlnmapTrnsportInfoInqire", params);
    }

    // 사전투표소 정보를 가져오는 메서드
    public ResponseEntity<Object> getPrePollingStationsData(Map<String, String> params) {
        return fetchData("/9760000/PolplcInfoInqireService2/getPrePolplcOtlnmapTrnsportInfoInqire", params);
    }

    // 외부 공공 API에 요청을 보내고, 그 결과를 프론트엔드(또는 다른 호출자)에게 반환하기 위한 공통 유틸 함수
    private ResponseEntity<Object> fetchData(String endpoint, Map<String, String> params) {
        try {
            // URI 빌더 생성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint);

            // 서비스 키 추가
            builder.queryParam("serviceKey", serviceKey);

            // 요청 파라미터 추가
            params.forEach((key, value) -> {
                if (!key.equals("serviceKey")) {  // Avoid duplicate serviceKey
                    builder.queryParam(key, value);
                }
            });

            URI uri = builder.build().encode().toUri();

            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching data: " + e.getMessage());
        }
    }

}
