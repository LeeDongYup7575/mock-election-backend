package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentCreateRequestDTO {
    private Long id;
    private Long postId;
    private Long parentId;
    private String authorId;
    private String content;
    private Integer depth;
}
