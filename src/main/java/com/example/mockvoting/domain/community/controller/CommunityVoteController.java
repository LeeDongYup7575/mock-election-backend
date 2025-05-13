package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.CommunityVoteRequestDTO;
import com.example.mockvoting.domain.community.service.CommunityVoteService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/community/votes")
@RequiredArgsConstructor
public class CommunityVoteController {
    private final CommunityVoteService communityVoteService;

    /**
     *  커뮤니티 투표 요청 처리
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> vote(@RequestBody CommunityVoteRequestDTO dto, HttpServletRequest request) {
        String voterId = (String) request.getAttribute("userId"); // 혹은 AuthenticationPrincipal 방식
        log.info("커뮤니티 투표 요청: targetType={}, targetId={}", dto.getTargetType(), dto.getTargetId());

        try{
            communityVoteService.processVote(dto, voterId);
            log.info("커뮤니티 투표 요청 처리 성공: targetType={}, targetId={}", dto.getTargetType(), dto.getTargetId());
            return ResponseEntity.ok(ApiResponse.success("커뮤니티 투표 성공", null));
        } catch (Exception e) {
            log.error("커뮤니티 투표 요청 처리 실패: targetType={}, targetId={}", dto.getTargetType(), dto.getTargetId());
            return ResponseEntity.internalServerError().body(ApiResponse.error("커뮤니티 투표 실패"));
        }
    }

}
