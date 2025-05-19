package com.example.mockvoting.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsDTO {
    private String title;
    private String content;
    private String imageUrl;
    private String link;
}
