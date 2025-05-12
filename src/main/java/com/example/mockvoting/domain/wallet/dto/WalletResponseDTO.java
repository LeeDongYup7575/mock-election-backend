package com.example.mockvoting.domain.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponseDTO {
    private String walletAddress;
    private int tokenBalance;
    private boolean connected;
}