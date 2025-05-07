package com.example.mockvoting.domain.candidate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Candidate {
    private int id;
    private String sgId;
    private String cnddtId;
    private String name;
    private String jdName;
    private String birthday;
    private String gender;
    private String edu;
    private String career1;
    private String career2;

}
