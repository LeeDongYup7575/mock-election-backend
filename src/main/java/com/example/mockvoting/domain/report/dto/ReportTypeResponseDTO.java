package com.example.mockvoting.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTypeResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
}
