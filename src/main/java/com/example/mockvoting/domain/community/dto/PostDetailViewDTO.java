package com.example.mockvoting.domain.community.dto;

import lombok.*;

@Value
@Builder
public class PostDetailViewDTO {    // PostController <- PostService 데이터 전달용 DTO
    PostDetailResponseDTO post;
    String newViewedPostIds;
}
