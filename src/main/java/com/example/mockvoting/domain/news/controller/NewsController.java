package com.example.mockvoting.domain.news.controller;

import com.example.mockvoting.domain.news.dto.NewsDTO;
import com.example.mockvoting.domain.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/newsdata")
    public ResponseEntity<List<NewsDTO>> getNews() {
        return ResponseEntity.ok(newsService.getNews());
    }
}
