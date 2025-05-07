package com.example.mockvoting.domain.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyPolicyDTO {
    private Long id;
    private String sgId;
    private String partyName;
    private Integer prmsOrd;
    private String realmName;
    private String title;
    private String content;
}
