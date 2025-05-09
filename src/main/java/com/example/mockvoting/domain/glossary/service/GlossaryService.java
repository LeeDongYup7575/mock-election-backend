package com.example.mockvoting.domain.glossary.service;

import com.example.mockvoting.domain.glossary.dto.GlossaryDTO;
import com.example.mockvoting.domain.glossary.entity.GlossaryTerm;
import com.example.mockvoting.domain.glossary.repository.GlossaryTermRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class GlossaryService {

    private final GlossaryTermRepository repo;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public GlossaryService(GlossaryTermRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public GlossaryDTO search(String term) {
        // 1) DB 검색
        Optional<GlossaryTerm> opt = repo.findByTermContainingIgnoreCase(term).stream().findFirst();
        if (opt.isPresent()) {
            GlossaryTerm gt = opt.get();
            return new GlossaryDTO(gt.getTerm(), gt.getDefinition(), "DB", null);
        }

        // 2) Wikipedia API로 직접 해당 용어의 정의를 요청 (page/summary)
        try {
            String urlTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);
            String url = "https://ko.wikipedia.org/api/rest_v1/page/summary/" + urlTerm;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            // 2.1) 정상 응답인 경우
            if (resp.statusCode() == 200) {
                JsonNode root = mapper.readTree(resp.body());
                String extract = root.path("extract").asText("");
                String pageUrl = root.path("content_urls")
                        .path("desktop")
                        .path("page")
                        .asText(null);
                if (!extract.isBlank()) {
                    return new GlossaryDTO(term, extract, "Wikipedia", pageUrl);
                }
            }

            // 2.2) 해당 용어가 없으면 검색 API를 통해 유사한 용어 검색
            if (resp.statusCode() == 404) {
                String searchUrl = "https://ko.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&format=json";
                HttpRequest searchReq = HttpRequest.newBuilder()
                        .uri(URI.create(searchUrl))
                        .header("Accept", "application/json")
                        .build();
                HttpResponse<String> searchResp = httpClient.send(searchReq, HttpResponse.BodyHandlers.ofString());

                if (searchResp.statusCode() == 200) {
                    JsonNode searchRoot = mapper.readTree(searchResp.body());
                    JsonNode searchResults = searchRoot.path("query").path("search");

                    // 2.3) 가장 첫 번째 검색된 용어로 다시 page/summary 호출
                    if (searchResults.isArray() && searchResults.size() > 0) {
                        String bestTitle = searchResults.get(0).path("title").asText();
                        String fallbackUrl = "https://ko.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(bestTitle, StandardCharsets.UTF_8);
                        HttpRequest fallbackReq = HttpRequest.newBuilder()
                                .uri(URI.create(fallbackUrl))
                                .header("Accept", "application/json")
                                .build();
                        HttpResponse<String> fallbackResp = httpClient.send(fallbackReq, HttpResponse.BodyHandlers.ofString());

                        if (fallbackResp.statusCode() == 200) {
                            JsonNode fallbackRoot = mapper.readTree(fallbackResp.body());
                            String fallbackExtract = fallbackRoot.path("extract").asText("");
                            String fallbackPageUrl = fallbackRoot.path("content_urls")
                                    .path("desktop")
                                    .path("page")
                                    .asText(null);
                            if (!fallbackExtract.isBlank()) {
                                return new GlossaryDTO(term, fallbackExtract, "Wikipedia", fallbackPageUrl);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            // 예외 처리 (로깅만)
            e.printStackTrace();
        }

        // 3) 둘 다 실패한 경우
        return null;
    }
}
