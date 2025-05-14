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
public class PostCommentResponseDTO {
    private Long id;
    private Long postId;
    private Long parentId;
    private String authorId;
    private String content;
    private Integer depth;
    private Integer voteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String authorNickname;
}
