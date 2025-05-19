package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.CommunityMainResponseDTO;
import com.example.mockvoting.domain.community.service.CommunityService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<CommunityMainResponseDTO>> getCommunityMainInfo() {
        log.info("커뮤니티 메인 정보 조회 요청");
        try {
            CommunityMainResponseDTO dto = communityService.getMainInfo();
            log.info("커뮤니티 메인 정보 조회 요청 처리 성공");
            return ResponseEntity.ok(ApiResponse.success("커뮤니티 메인 정보 조회 요청 처리 성공", dto));
        } catch (Exception e) {
            log.error("커뮤니티 메인 정보 조회 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("커뮤니티 메인 정보 조회 요청 처리 실패"));
        }
    }
}
