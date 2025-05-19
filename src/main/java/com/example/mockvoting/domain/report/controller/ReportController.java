package com.example.mockvoting.domain.report.controller;

import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.dto.ReportDTO;
import com.example.mockvoting.domain.report.entity.Report;
import com.example.mockvoting.domain.report.service.ReportService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/lists")
    public ResponseEntity<ApiResponse<List<ReportDTO>>> getAllReports() {
        log.info("전체 신고 내역 조회 요청");

        try {
            List<ReportDTO> reports = reportService.getAllReports();
            log.info("신고 내역 {}건 조회됨", reports.size());
            return ResponseEntity.ok(ApiResponse.success("신고 내역 조회 성공", reports));
        } catch (Exception e) {
            log.error("신고 내역 조회 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("신고 내역 조회 실패"));
        }
    }

    // 상세 조회
    @GetMapping("/lists/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> getReportById(@PathVariable Long id) {
        ReportDTO dto = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("신고 상세 조회 성공", dto));
    }

    // 처리(확인) 업데이트
    @PatchMapping("/lists/{id}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReport(@PathVariable Long id) {
        reportService.confirmReport(id);
        return ResponseEntity.ok(ApiResponse.success("신고 처리(확인) 성공", null));
    }

    /**
     * 일별 신고 통계
     */
    @GetMapping("/stats/daily")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyStats() {
        List<Map<String, Object>> stats = reportService.countReportsByDay();
        return ResponseEntity.ok(ApiResponse.success("일별 신고 통계 조회 성공", stats));
    }

    /**
     * 주별 신고 통계
     */
    @GetMapping("/stats/weekly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWeeklyStats() {
        List<Map<String, Object>> stats = reportService.countReportsByWeek();
        return ResponseEntity.ok(ApiResponse.success("주별 신고 통계 조회 성공", stats));
    }

    /**
     * 월별 신고 통계
     */
    @GetMapping("/stats/monthly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyStats() {
        List<Map<String, Object>> stats = reportService.countReportsByMonth();
        return ResponseEntity.ok(ApiResponse.success("월별 신고 통계 조회 성공", stats));
    }


}
