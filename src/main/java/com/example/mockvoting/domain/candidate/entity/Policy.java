package com.example.mockvoting.domain.candidate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
    private int id;
    private String sgId;
    private String partyName;
    private int prmsOrd;
    private String realName;
    private String title;
    private String content;
}
