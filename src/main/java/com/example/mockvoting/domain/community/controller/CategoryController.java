package com.example.mockvoting.domain.community.controller;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.service.CategoryService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
