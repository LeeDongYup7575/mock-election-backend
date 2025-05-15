package com.example.mockvoting.domain.report.dto;

import com.example.mockvoting.domain.community.entity.CommunityVote;
import com.example.mockvoting.domain.report.entity.Report;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequestDTO {
    private Long reportTypeId;
    private Report.TargetType targetType;
    private Long targetId;
    @Size(max = 300, message = "신고 사유는 300자 이하여야 합니다.")
    private String reason;
}
