package com.example.mockvoting.domain.news.service;

import com.example.mockvoting.domain.candidate.entity.Candidate;
import com.example.mockvoting.domain.news.crawler.NewsCrawler;
import com.example.mockvoting.domain.news.dto.NewsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService {
    @Autowired
    private NewsCrawler newsCrawler;

    public List<NewsDTO> getNews() {
        return newsCrawler.fetchLatestNews();
    }

}
