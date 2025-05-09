package com.example.mockvoting.domain.voting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteHistory {
    private Long id;
    private String userId;
    private String sgId;          // 선거 ID
    private Integer candidateId;  // 어떤 후보에 투표했는지 (익명화될 수 있음)
    private LocalDateTime votedAt;
}