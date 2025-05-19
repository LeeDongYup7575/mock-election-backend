package com.example.mockvoting.domain.report.mapper;

import com.example.mockvoting.domain.report.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper {
    
    // 중복 신고 여부 조회
    boolean selectReportExistsByReporterAndTarget(
            @Param("reporterId") String reporterId,
            @Param("targetType") Report.TargetType targetType,
            @Param("targetId") Long targetId
    );
}
