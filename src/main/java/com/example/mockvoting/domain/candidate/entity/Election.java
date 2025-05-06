package com.example.mockvoting.domain.candidate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Election {
    private int id;
    private String sgId;
    private String sgName;
    private String sgTypeCode;
}
