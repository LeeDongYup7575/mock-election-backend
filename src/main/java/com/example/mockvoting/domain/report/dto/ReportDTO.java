package com.example.mockvoting.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private int id;
    private String reportTypeName;
    private String reporterId;
    private String reportedUserId;
    private String targetType;
    private int targetId;
    private String reason;
    private Boolean isConfirmed;
    private String reportedAt;
}
