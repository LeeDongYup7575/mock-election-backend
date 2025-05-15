package com.example.mockvoting.domain.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingStats {
    private Long id;
    private String sgId;
    private Integer candidateId;
    private Long policyId;
    private Integer voteCount;
    private Double percentage;
}