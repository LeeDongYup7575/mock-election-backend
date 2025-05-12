package com.example.mockvoting.domain.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    private Long id;
    private String userId;
    private String walletAddress;
    private String privateKey;
    private int tokenBalance;
    private String walletType; // "INTERNAL" 또는 "METAMASK"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}