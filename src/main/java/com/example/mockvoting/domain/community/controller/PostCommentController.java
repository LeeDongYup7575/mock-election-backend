package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.PostCommentCreateRequestDTO;
import com.example.mockvoting.domain.community.dto.PostCommentResponseDTO;
import com.example.mockvoting.domain.community.entity.PostComment;
import com.example.mockvoting.domain.community.service.PostCommentService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    /**
     *  최상위 댓글 조회
     */
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<PostCommentResponseDTO>>> getTopLevelComments(
//            @PathVariable Long postId,
//            @RequestParam(defaultValue = "0") int offset,
//            @RequestParam(defaultValue = "10") int limit) {
//
//        log.info("댓글 목록 요청: postId={}, offset={}, limit={}", postId, offset, limit);
//
//        try {
//            List<PostCommentResponseDTO> comments = postCommentService.getTopLevelCommentsByPostId(postId, offset, limit);
//            log.info("댓글 목록 요청 처리 성공: postId={}", postId);
//            return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 성공", comments));
//        } catch (Exception e) {
//            log.error("댓글 목록 요청 처리 실패", e);
//            return ResponseEntity.internalServerError().body(ApiResponse.error("댓글 목록 조회 실패"));
//        }
//    }

    /**
     *  댓글 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostCommentResponseDTO>>> getAllComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("댓글 목록 요청: postId={}, offset={}, limit={}", postId, offset, limit);
        try {
            List<PostCommentResponseDTO> comments = postCommentService.getCommentsWithReplies(postId, offset, limit);
            log.info("댓글 목록 요청 처리 성공: postId={}", postId);
            return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 성공", comments));
        } catch (Exception e) {
            log.error("댓글 목록 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("댓글 목록 조회 실패"));
        }
    }

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

    /**
     *  댓글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long postId, @PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("댓글 삭제 요청: postId={}, commentId={}, 요청자={}", postId, id, userId);
        try {
            postCommentService.delete(id, userId);
            log.info("댓글 삭제 성공: commentId={}", id);
            return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공", null));
        } catch (SecurityException e) {
            log.warn("댓글 삭제 권한 없음: commentId={}, 요청자={}", id, userId);
            return ResponseEntity.status(403).body(ApiResponse.error("댓글 삭제 권한이 없습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("댓글 삭제 실패: commentId={}, 요청자={}", id, userId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("댓글 삭제 실패"));
        }
    }
}
