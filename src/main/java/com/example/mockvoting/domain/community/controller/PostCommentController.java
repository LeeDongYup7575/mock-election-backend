package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.PostCommentCreateRequestDTO;
import com.example.mockvoting.domain.community.entity.PostComment;
import com.example.mockvoting.domain.community.service.PostCommentService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    /**
     *  댓글 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@PathVariable Long postId, @RequestBody PostCommentCreateRequestDTO dto) {
        log.info("댓글 등록 요청: postId={}, 요청자={}, 댓글 내용={}", postId, dto.getAuthorId(), dto.getContent());

        try {
            Long commentId = postCommentService.save(postId, dto);
            log.info("댓글 등록 요청 처리 성공: id={}", commentId);
            return ResponseEntity.ok(ApiResponse.success("댓글 등록 성공", commentId));
        } catch (Exception e) {
            log.error("댓글 등록 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("댓글 등록 실패"));
        }
    }
}
