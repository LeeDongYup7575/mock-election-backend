package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.service.CategoryService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/community/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     *  게시글 카테고리 전체 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        log.info("카테고리 전체 조회 요청");

        try {
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();
            log.info("카테고리 전체 조회 성공");
            return ResponseEntity.ok(ApiResponse.success("카테고리 전체 조회 성공", categories));
        } catch (Exception e) {
            log.error("카테고리 전체 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("카테고리 전체 조회 실패"));
        }
    }

    @GetMapping("/isActive")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategoriesByIsActive() {
        log.info("활성화 카테고리 조회 요청");

        try {
            List<CategoryResponseDTO> categories = categoryService.getCategoriesByIsActive();
            log.info("활성화 카테고리 조회 성공");
            return ResponseEntity.ok(ApiResponse.success("활성화 카테고리 조회 성공", categories));
        } catch (Exception e) {
            log.error("활성화 카테고리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("활성화 카테고리 조회 실패"));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getPostCountByCategory(@RequestParam String categoryCode) {
        log.info("카테고리별 게시글 수 조회 요청: {}", categoryCode);
        try {
            int count = categoryService.selectPostCountByCategory(categoryCode);
            log.info("카테고리별 게시글 수 조회 성공: {} -> {}", categoryCode, count);
            return ResponseEntity.ok(ApiResponse.success("카테고리별 게시글 수 조회 성공", count));
        } catch (Exception e) {
            log.error("카테고리별 게시글 수 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("카테고리별 게시글 수 조회 실패"));
        }
    }

    // 카테고리 상태 업데이트
    @PatchMapping("/{categoryId}/status")
    public ResponseEntity<?> updateCategoryStatus(
            @PathVariable Long categoryId,
            @RequestBody Map<String, Boolean> statusMap) {
        Boolean isActive = statusMap.get("isActive");
        categoryService.updateCategoryStatus(categoryId, isActive);
        return ResponseEntity.ok().body("카테고리 상태가 변경되었습니다.");
    }

    // 새 카테고리 추가
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody CategoryResponseDTO categoryDTO) {
        categoryService.addCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("카테고리가 추가되었습니다.");
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok("카테고리가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("카테고리 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("카테고리 삭제 중 오류가 발생했습니다.");
        }
    }


}
