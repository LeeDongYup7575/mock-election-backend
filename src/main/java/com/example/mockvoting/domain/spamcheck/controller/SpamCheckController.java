package com.example.mockvoting.domain.spamcheck.controller;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.spamcheck.model.SpamCheckType;
import com.example.mockvoting.domain.spamcheck.service.SpamCheckService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/spam-check")
@RequiredArgsConstructor
public class SpamCheckController {
    private final SpamCheckService spamCheckService;

    /**
     * 도배 의심 여부 판단 (게시글/댓글 공용)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> checkSpam(@RequestParam SpamCheckType type,
                                                          @RequestParam(required = false) String title,
                                                          @RequestParam String content,
                                                          HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        log.info("도배 판단 요청: userId={}, type={}", userId, type);

        try {
            boolean suspicious = spamCheckService.isSuspicious(userId, type, title, content);
            log.info("도배 판단 요청 처리 성공: isSuspicious={}", suspicious);
            return ResponseEntity.ok(ApiResponse.success("도배 판단 성공", suspicious));
        } catch (Exception e) {
            log.error("도배 판단 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("도배 판단 실패"));
        }
    }
}
