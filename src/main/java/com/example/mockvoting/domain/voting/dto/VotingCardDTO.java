package com.example.mockvoting.domain.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingCardDTO {
    private String sgId;
    private String title;
    private String date;
    private String description;
}