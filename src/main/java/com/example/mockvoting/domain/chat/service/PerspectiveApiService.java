package com.example.mockvoting.domain.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PerspectiveApiService {

    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final List<String> profanityList; //한국어 비속어 목록

    // application.properties 에서 API 키를 가져옵니다.
    public PerspectiveApiService(@Value("${perspective.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://commentanalyzer.googleapis.com/v1alpha1")
                .build();
        this.objectMapper = new ObjectMapper();
        this.profanityList = new ArrayList<>();
    }

    // 서비스 초기화 시 JSON 파일에서 비속어 목록 로드
    @PostConstruct
    public void init() {
        try {
            loadProfanityListFromJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // JSON 파일에서 비속어 목록 로드
    private void loadProfanityListFromJson() throws IOException {
        try{
            // 클래스패스 리소스에서 JSON 파일 로드
            ClassPathResource resource = new ClassPathResource("profanity/profanity.json");
            InputStream inputStream = resource.getInputStream();

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode profanityNode = rootNode.get("profanity");

            // 기존 리스트 비우기
            profanityList.clear();

            // 비속어 목록 추출
            if (profanityNode.isArray()) {
                for( JsonNode node : profanityNode ){
                    profanityList.add(node.asText());
                }
            }

            // 리소스 닫기
            inputStream.close();
        } catch (IOException e) {
            throw e;
        }
    }

    // 입력된 텍스트가 Perspective API 기준으로 독성(비속어 등) 콘텐츠인지 여부를 판별
    public Mono<Boolean> containsToxicContent(String text) {

        // 텍스트가 비어있거나 너무 짧으면 검사하지 않음
        if (text == null) {
            return Mono.just(false);
        }

        // 커스텀 비속어 필터링 (JSON에서 로드한 한국어 비속어 목록 체크)
        String lowerText = text.toLowerCase();

        // 비속어 검사 로직 강화
        boolean found = false;
        for ( String word : profanityList ) {
            if(lowerText.contains(word.toLowerCase())) {
                return Mono.just(true);
            }
        }

        // Perspective API에 보낼 요청 본문 생성
        String requestBody = buildRequestBody(text);
        System.out.println("Perspective API 요청: " + requestBody);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/comments:analyze")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Perspective API 응답: " + response))
                .map(this::parseToxicityScore)
                .map(score -> {
                    System.out.println("비속어 점수: " + score);
                    return score > 0.7;
                })// 0.7 이상이면 독성 컨텐츠로 간주 (임계값 설정 가능)
                .onErrorReturn(false); // 오류 발생 시 기본값
    }

    // Perspective API에 전달할 JSON 요청 본문을 생성하는 함수
    private String buildRequestBody(String text) {
        JsonObject requestBodyJson = new JsonObject();

        JsonObject commentJson = new JsonObject();

        commentJson.addProperty("text", text);
        requestBodyJson.add("comment", commentJson);

        requestBodyJson.add("languages", new Gson().toJsonTree(List.of("ko"))); // 언어설정

        JsonObject requestedAttributesJson = new JsonObject();
        requestedAttributesJson.add("TOXICITY", new JsonObject());
        requestBodyJson.add("requestedAttributes", requestedAttributesJson);

        return requestBodyJson.toString();
    }

    // Perspective API에서 받은 JSON 응답에서 toxicity 점수(double)를 안전하게 파싱
    private double parseToxicityScore(String responseBody) {
        try {
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            return responseJson
                    .getAsJsonObject("attributeScores")
                    .getAsJsonObject("TOXICITY")
                    .getAsJsonObject("summaryScore")
                    .get("value").getAsDouble();
        } catch (Exception e) {
            e.printStackTrace();
            // JSON 파싱 오류 발생 시 기본값 반환
            return 0.0;

        }
    }


}
