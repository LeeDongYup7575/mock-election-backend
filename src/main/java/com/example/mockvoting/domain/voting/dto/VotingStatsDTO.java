package com.example.mockvoting.domain.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingStatsDTO {
    private String sgId;
    private Double participation;
    private List<CandidateVoteDTO> votes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateVoteDTO {
        private Integer candidateId;
        private Double percentage;
        private Integer voteCount;
    }
}