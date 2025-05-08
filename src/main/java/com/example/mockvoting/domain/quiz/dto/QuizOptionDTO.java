package com.example.mockvoting.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionDTO {
    private Long id;
    private Long quizId;
    private String optionText;
    private Boolean isCorrect;
    // 옵션 번호 (1,2,3,4 중 하나)
    private Integer optionNumber;
}