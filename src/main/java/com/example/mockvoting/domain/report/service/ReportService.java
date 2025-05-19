package com.example.mockvoting.domain.report.service;

import com.example.mockvoting.domain.community.repository.PostCommentRepository;
import com.example.mockvoting.domain.community.repository.PostRepository;
import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.entity.Report;
import com.example.mockvoting.domain.report.mapper.ReportMapper;
import com.example.mockvoting.domain.report.mapper.converter.ReportDtoMapper;
import com.example.mockvoting.domain.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
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
}
