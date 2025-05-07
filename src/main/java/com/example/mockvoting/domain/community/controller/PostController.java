package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.service.PostService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     *  게시글 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponseDTO>> getPostDetail(@PathVariable Integer id) {
        log.info("게시글 [{}] 상세 조회 요청", id);

        try {
            PostDetailResponseDTO post = postService.getPostDetail(id);
            log.info("게시글 [{}] 상세 조회 성공", id);
            return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회 성공", post));
        } catch (Exception e) {
            log.error("게시글 [{}] 상세 조회 중 오류 발생", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("게시글 상세 조회 실패"));
        }
    }
}
