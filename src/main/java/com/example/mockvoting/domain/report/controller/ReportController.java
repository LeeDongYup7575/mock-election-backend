package com.example.mockvoting.domain.report.controller;

import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.service.ReportService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
