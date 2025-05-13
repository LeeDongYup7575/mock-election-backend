package com.example.mockvoting.domain.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaMaskVotingDTO {
    private Integer candidateId;
    private String transactionHash; // 블록체인 트랜잭션 해시
}