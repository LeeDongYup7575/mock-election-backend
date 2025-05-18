package com.example.mockvoting.domain.report.controller;

import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.entity.Report;
import com.example.mockvoting.domain.report.service.ReportService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    /**
     *  신고 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReport(@RequestBody @Valid ReportCreateRequestDTO dto, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("신고 등록 요청: targetType={}, targetId={} 요청자={}", dto.getTargetType(), dto.getTargetId(), userId);
        try {
            reportService.create(dto, userId);
            log.info("신고 등록 요청 처리 성공");
            return ResponseEntity.ok(ApiResponse.success("신고 등록 성공", null));
        } catch (Exception e) {
            log.error("신고 등록 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("신고 등록 실패"));
        }
    }

    /**
     *  중복 신고 여부 조회
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkReportExists(@RequestParam("targetType") Report.TargetType targetType, @RequestParam("targetId") Long targetId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("중복 신고 여부 조회 요청: targetType={}, targetId={} 요청자={}", targetType, targetId, userId);

        try{
            log.info("중복 신고 여부 조회 처리 성공");
            boolean exists = reportService.checkExists(userId, targetType, targetId);
            return ResponseEntity.ok(ApiResponse.success("신고 여부 확인 성공", exists));
        } catch (Exception e) {
            log.error("중복 신고 여부 조회 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("신고 여부 확인 실패"));
        }
    }

}
