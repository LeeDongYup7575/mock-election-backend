package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.PostCreateRequestDTO;
import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import com.example.mockvoting.domain.community.service.PostService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            log.info("게시글 [{}] 상세 조회 요청 처리 성공", id);
            return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회 성공", post));
        } catch (Exception e) {
            log.error("게시글 [{}] 상세 조회 요청 처리 실패", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("게시글 상세 조회 실패"));
        }
    }

    /**
     *  카테고리별 게시글 조회
     */
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<Page<PostSummaryResponseDTO>>> getPostsByCategory(@PathVariable String categoryCode, Pageable pageable) {
        log.info("카테고리 [{}]에 해당하는 게시글 목록 조회 요청", categoryCode);

        try {
            Page<PostSummaryResponseDTO> posts = postService.getPostsByCategory(categoryCode, pageable);
            log.info("카테고리 [{}] 게시글 목록 조회 요청 처리 성공", categoryCode);
            return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", posts));
        } catch (Exception e) {
            log.error("카테고리 [{}] 게시글 목록 조회 요청 처리 실패", categoryCode, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("게시글 목록 조회 실패"));
        }
    }

    /**
     *  게시글 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@RequestBody PostCreateRequestDTO dto) {
        log.info("게시글 등록 요청: {}", dto.getTitle());

        try {
            Long postId = postService.save(dto);
            log.info("게시글 등록 요청 처리 성공: id={}", postId);
            return ResponseEntity.ok(ApiResponse.success("게시글 등록 성공", postId));
        } catch (Exception e) {
            log.error("게시글 등록 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("게시글 등록 실패"));
        }
    }

}
