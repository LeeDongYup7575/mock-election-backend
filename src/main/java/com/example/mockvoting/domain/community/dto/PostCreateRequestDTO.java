package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {
    private Long id;
    private Long categoryId;
    private String title;
    private String content;
    private String authorId;
    private String thumbnailUrl;
}
