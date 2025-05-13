package com.example.mockvoting.domain.policyQuestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyQuestionSelectDTO {
    private int id;
    private String userId;
    private int questionId;
    private int selectOptionId;


}
