package com.example.mockvoting.domain.report.service;

import com.example.mockvoting.domain.community.repository.PostCommentRepository;
import com.example.mockvoting.domain.community.repository.PostRepository;
import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.dto.ReportDTO;
import com.example.mockvoting.domain.report.entity.Report;
import com.example.mockvoting.domain.report.mapper.ReportMapper;
import com.example.mockvoting.domain.report.mapper.converter.ReportDtoMapper;
import com.example.mockvoting.domain.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    // 사용자 비활성화 임계값 (확인된 신고 횟수)
    private static final int DEACTIVATION_THRESHOLD = 3;

    private final ReportMapper reportMapper;
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final ReportDtoMapper reportDtoMapper;

    /**
     *  신고 등록
     */
    public void create(ReportCreateRequestDTO dto, String reporterId) {
        String reportedUserId = findReportedUserId(dto.getTargetType(), dto.getTargetId());

        Report report = reportDtoMapper.toEntity(dto);
        report.setReporterId(reporterId);
        report.setReportedUserId(reportedUserId);

        reportRepository.save(report);
    }

    private String findReportedUserId(Report.TargetType type, Long id) {
        return switch (type) {
            case POST -> postRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."))
                    .getAuthorId();

            case POST_COMMENT -> postCommentRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."))
                    .getAuthorId();

            // case FEED ->
            // case FEED_COMMENT ->
            // case CHAT ->

            default -> throw new IllegalArgumentException("지원하지 않는 신고 대상 타입입니다.");
        };
    }

    /**
     *  중복 신고 여부 조회
     */
    public boolean checkExists(String reporterId, Report.TargetType targetType, Long targetId) {
        return reportMapper.selectReportExistsByReporterAndTarget(
                reporterId,
                targetType,
                targetId
        );
    }

    public List<ReportDTO> getAllReports() {
        return reportMapper.getAllReports();
    }

    public ReportDTO getReportById(Long id) {
        return reportMapper.getReportById(id);
    }

    @Transactional
    public void confirmReport(Long id) {
        // 신고 확인 처리
        reportMapper.updateConfirmed(id, true);

        // 처리된 신고 정보 조회
        ReportDTO report = reportMapper.getReportById(id);
        String reportedUserId = report.getReportedUserId();

        // 해당 사용자에 대한 확인된 신고 수 조회
        int confirmedReportCount = reportMapper.countConfirmedReportsAgainstUser(reportedUserId);

        // 임계값(3회) 이상 신고되었다면 사용자 비활성화
        if (confirmedReportCount >= DEACTIVATION_THRESHOLD) {
            log.info("사용자 ID: {} - {}회 이상 신고되어 계정이 비활성화됩니다.", reportedUserId, DEACTIVATION_THRESHOLD);
            reportMapper.deactivateUser(reportedUserId);
        }
    }

    /**
     * 일별 신고 건수 조회
     */
    public List<Map<String, Object>> countReportsByDay() {
        return reportMapper.countReportsByDay();
    }

    /**
     * 주별 신고 건수 조회
     */
    public List<Map<String, Object>> countReportsByWeek() {
        return reportMapper.countReportsByWeek();
    }

    /**
     * 월별 신고 건수 조회
     */
    public List<Map<String, Object>> countReportsByMonth() {
        return reportMapper.countReportsByMonth();
    }
}
