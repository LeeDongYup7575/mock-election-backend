package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.*;
import com.example.mockvoting.domain.community.service.PostService;
import com.example.mockvoting.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.Cookie;

import java.util.List;

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
    public ResponseEntity<ApiResponse<PostDetailResponseDTO>> getPostDetail(
            @PathVariable Long id,
            @RequestParam(value = "userId", required = false) String userId
//            @CookieValue(value = "viewedPostIds", defaultValue = "") String viewedPostIds,
//            HttpServletResponse response
    ) {

        log.info("게시글 [{}] 상세 조회 요청", id);

        try {
//            PostDetailViewDTO result = postService.getPostDetail(id, viewedPostIds, userId);
            PostDetailViewDTO result = postService.getPostDetail(id, userId);

            // 새 쿠키값이 있을 때만 HTTP 응답에 추가
//            if (result.getNewViewedPostIds() != null) {
//                Cookie cookie = new Cookie("viewedPostIds", result.getNewViewedPostIds());
//                cookie.setPath("/");
//                cookie.setMaxAge(60 * 60 * 12); // 12시간 유지
//                response.addCookie(cookie);
//            }

            log.info("게시글 [{}] 상세 조회 요청 처리 성공", id);
            return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회 성공", result.getPost()));
        } catch (Exception e) {
            log.error("게시글 [{}] 상세 조회 요청 처리 실패", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("게시글 상세 조회 실패"));
        }
    }

    /**
     * 게시글 상세 조회(수정용)
     */
    @GetMapping("/{id}/edit")
    public ResponseEntity<ApiResponse<PostDetailResponseDTO>> getPostForEdit(@PathVariable Long id, HttpServletRequest request) {
        String requesterId = (String) request.getAttribute("userId");
        log.info("게시글 상세 조회(수정용) 요청: postId={}, 요청자={}", id, requesterId);

        try {
            PostDetailResponseDTO dto = postService.getPostForEdit(id, requesterId);
            log.info("게시글 상세 조회(수정용) 요청 처리 성공: postId={}", id);
            return ResponseEntity.ok(ApiResponse.success("게시글 수정용 조회 성공", dto));
        } catch (AccessDeniedException e) {
            log.warn("게시글 상세 조회(수정용) 권한 없음: postId={}, 요청자={}", id, requesterId);
            return ResponseEntity.status(403).body(ApiResponse.error("게시글 상세 조회(수정용) 권한이 없습니다."));
        } catch (Exception e) {
            log.error("게시글 상세 조회(수정용) 요청 처리 실패: postId={}, 요청자={}", id, requesterId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("조회 실패"));
        }
    }

    /**
     *  카테고리별 게시글 조회
     *  categoryCode == all -> 전체 조회
     *  else -> 카테고리별 조회
     */
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<Page<PostSummaryResponseDTO>>> getPostsByCategory(
            @PathVariable String categoryCode,
            Pageable pageable,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword) {
        log.info("카테고리 [{}]에 해당하는 게시글 목록 조회 요청", categoryCode);

        try {
            Page<PostSummaryResponseDTO> posts = postService.getPostsByCategory(categoryCode, pageable, searchType, keyword);
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> create(@RequestPart PostCreateRequestDTO dto,
                                                    @RequestPart(value="attachments", required = false) List<MultipartFile> attachments,
                                                    HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("userId");
        log.info("게시글 등록 요청: {}", userId);

        try {
            Long postId = postService.save(dto, attachments, userId);
            log.info("게시글 등록 요청 처리 성공: id={}", postId);
            return ResponseEntity.ok(ApiResponse.success("게시글 등록 성공", postId));
        } catch (Exception e) {
            log.error("게시글 등록 요청 처리 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("게시글 등록 실패"));
        }
    }

    /**
     *  게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, HttpServletRequest request) {
        String requesterId = (String) request.getAttribute("userId");
        log.info("게시글 삭제 요청: postId={}, 요청자={}", id, requesterId);

        try {
            postService.delete(id, requesterId);
            log.info("게시글 삭제 요청 처리 성공: postId={}", id);
            return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공", null));
        } catch (SecurityException e) {
            log.warn("게시글 삭제 권한 없음: postId={}, 요청자={}", id, requesterId);
            return ResponseEntity.status(403).body(ApiResponse.error("게시글 삭제 권한이 없습니다."));
        } catch (Exception e) {
            log.error("게시글 삭제 요청 처리 실패: postId={}, 요청자={}", id, requesterId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("게시글 삭제 실패"));
        }
    }

    /**
     *  게시글 수정
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @RequestPart PostUpdateRequestDTO dto,
                                                    @RequestPart(value="attachments", required = false) List<MultipartFile> attachments, HttpServletRequest request) {
        String requesterId = (String) request.getAttribute("userId");
        log.info("게시글 수정 요청: postId={}, 요청자={}", id, requesterId);

        try {
            postService.update(id, dto, requesterId, attachments);
            log.info("게시글 수정 요청 처리 성공: postId={}, 요청자={}", id, requesterId);
            return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", null));
        } catch (AccessDeniedException e) {
            log.warn("게시글 수정 권한 없음: postId={}, 요청자={}", id, requesterId);
            return ResponseEntity.status(403).body(ApiResponse.error("수정 권한이 없습니다."));
        } catch (Exception e) {
            log.error("게시글 수정 요청 처리 실패: postId={}, 요청자={}", id, requesterId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("게시글 수정 실패"));
        }
    }

}
