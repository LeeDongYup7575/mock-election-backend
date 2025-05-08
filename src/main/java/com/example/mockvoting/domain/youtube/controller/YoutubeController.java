package com.example.mockvoting.domain.youtube.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/youtube")
public class YoutubeController {

    private final String API_KEY = "AIzaSyC3FObkMCKAl8mbzGEuG3ox82CWJgAXA4s";

    @GetMapping("/videos")
    public ResponseEntity<?> getYoutubeVideos(@RequestParam String query) {
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&q=" + query
                + "&key=" + API_KEY
                + "&type=video"
                + "&maxResults=10";

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유튜브 API 호출 실패");
        }
    }
}
