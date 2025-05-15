package com.example.mockvoting.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentUpdateRequestDTO {
    @NotBlank
    @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다.")
    private String content;
}
