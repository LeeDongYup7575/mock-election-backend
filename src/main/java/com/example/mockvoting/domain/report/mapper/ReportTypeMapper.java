package com.example.mockvoting.domain.report.mapper;

import com.example.mockvoting.domain.report.dto.ReportTypeResponseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportTypeMapper {

    // 신고 유형 전체 조회
    List<ReportTypeResponseDTO> selectAllReportTypes();
}
