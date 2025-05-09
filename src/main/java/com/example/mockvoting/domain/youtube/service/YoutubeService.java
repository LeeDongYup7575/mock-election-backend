package com.example.mockvoting.domain.youtube.service;

import com.example.mockvoting.domain.youtube.entity.YoutubeCache;
import com.example.mockvoting.domain.youtube.repository.YoutubeCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class YoutubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final YoutubeCacheRepository cacheRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Duration ttl = Duration.ofMinutes(30); // TTL: 30분

    public YoutubeService(YoutubeCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public String getVideos(String query) {
        Optional<YoutubeCache> optionalCache = cacheRepository.findByQuery(query);

        if (optionalCache.isPresent()) {
            YoutubeCache cache = optionalCache.get();
            if (cache.getCachedAt().isAfter(LocalDateTime.now().minus(ttl))) {
                return cache.getResponse(); // ✅ 캐시 유효, 반환
            }
        }

        // ⛔ 캐시 없음 or 만료 → API 호출
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&q=" + query
                + "&key=" + apiKey
                + "&type=video"
                + "&maxResults=10";

        String response = restTemplate.getForObject(url, String.class);

        YoutubeCache newCache = optionalCache.orElse(new YoutubeCache());
        newCache.setQuery(query);
        newCache.setResponse(response);
        newCache.setCachedAt(LocalDateTime.now());
        cacheRepository.save(newCache);

        return response;
    }
}
