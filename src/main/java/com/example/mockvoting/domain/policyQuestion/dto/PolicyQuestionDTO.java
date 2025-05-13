package com.example.mockvoting.domain.policyQuestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyQuestionDTO {
    private int id;
    private String question;
    private List<PolicyQuestionOptionsDTO> options;
    private Integer userSelectedOptionId; // 사용자가 선택한 옵션 ID (null 가능)
}