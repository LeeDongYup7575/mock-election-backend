package com.example.mockvoting.domain.report.mapper;

import com.example.mockvoting.domain.report.dto.ReportDTO;
import com.example.mockvoting.domain.report.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    
    // 중복 신고 여부 조회
    boolean selectReportExistsByReporterAndTarget(
            @Param("reporterId") String reporterId,
            @Param("targetType") Report.TargetType targetType,
            @Param("targetId") Long targetId
    );

    List<ReportDTO> getAllReports();

    ReportDTO getReportById(@Param("id") Long id);

    int updateConfirmed(@Param("id") Long id,
                        @Param("confirmed") boolean confirmed);

    List<Map<String, Object>> countReportsByDay();
    List<Map<String, Object>> countReportsByWeek();
    List<Map<String, Object>> countReportsByMonth();
}
