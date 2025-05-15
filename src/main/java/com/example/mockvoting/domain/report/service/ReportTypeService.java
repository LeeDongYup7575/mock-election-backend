package com.example.mockvoting.domain.report.service;

import com.example.mockvoting.domain.report.dto.ReportTypeResponseDTO;
import com.example.mockvoting.domain.report.mapper.ReportTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportTypeService {
    private final ReportTypeMapper reportTypeMapper;

    /**
     *  신고 유형 전체 조회
     */
    public List<ReportTypeResponseDTO> getAllReportTypes(){
        return reportTypeMapper.selectAllReportTypes();
    }

}
