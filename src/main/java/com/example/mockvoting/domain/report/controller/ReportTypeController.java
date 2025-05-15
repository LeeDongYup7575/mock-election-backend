package com.example.mockvoting.domain.report.controller;

import com.example.mockvoting.domain.report.dto.ReportTypeResponseDTO;
import com.example.mockvoting.domain.report.service.ReportTypeService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report-types")
@RequiredArgsConstructor
public class ReportTypeController {
    private final ReportTypeService reportTypeService;

    /**
     *  신고 유형 전체 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportTypeResponseDTO>>> getAllReportTypes() {
        log.info("신고 유형 목록 조회 요청");
        try {
            List<ReportTypeResponseDTO> reportTypes = reportTypeService.getAllReportTypes();
            return ResponseEntity.ok(ApiResponse.success("신고 유형 목록 조회 요청 처리 성공", reportTypes));
        } catch (Exception e) {
            log.error("신고 유형 목록 조회 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("신고 유형 목록 조회 실패"));
        }
    }

}
