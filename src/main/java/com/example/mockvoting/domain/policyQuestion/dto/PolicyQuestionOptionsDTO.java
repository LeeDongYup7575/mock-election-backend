package com.example.mockvoting.domain.policyQuestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyQuestionOptionsDTO {
    private int id;
    private int questionId;
    private String options;
    private int count;
    private double percent;  // 백분율 계산을 위한 필드 (DB에는 없음)
}
