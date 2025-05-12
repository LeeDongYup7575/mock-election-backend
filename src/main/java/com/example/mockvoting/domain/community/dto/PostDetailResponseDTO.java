package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponseDTO {
    private Integer id;
    private Integer categoryId;
    private String title;
    private String content;
    private String authorId;
    private Integer voteCount;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryCode;
    private String categoryName;
    private String authorNickname;
    private List<PostAttachmentResponseDTO> attachments;
    private Integer commentCount;
}
