package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponseDTO {
    private Long id;
    private Long categoryId;
    private String title;

    private String thumbnailUrl;
    private Integer voteCount;
    private Integer views;
    private LocalDateTime createdAt;

    private String categoryName;
    private String authorNickname;
    private Integer commentCount;
}
