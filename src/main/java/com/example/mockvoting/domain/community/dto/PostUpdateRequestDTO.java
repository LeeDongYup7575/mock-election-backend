package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private List<Long> deleteAttachmentIds;
}
