package com.example.mockvoting.domain.youtube.controller;

import com.example.mockvoting.domain.youtube.service.YoutubeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/youtube")
public class YoutubeController {

    private final YoutubeService youtubeService;

    public YoutubeController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @GetMapping("/videos")
    public ResponseEntity<?> getYoutubeVideos(@RequestParam String query) {
        try {
            String response = youtubeService.getVideos(query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유튜브 API 호출 실패: " + e.getMessage());
        }
    }
}

