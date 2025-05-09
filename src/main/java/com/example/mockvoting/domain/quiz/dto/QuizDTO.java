package com.example.mockvoting.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;
    private String question;
    private String explanation;

    @Builder.Default
    private List<QuizOptionDTO> options = new ArrayList<>();

    // 프론트엔드 호환성을 위한 필드
    private Integer correctAnswer;

    // 옵션 추가 메서드
    public void addOption(QuizOptionDTO option) {
        this.options.add(option);
    }
}