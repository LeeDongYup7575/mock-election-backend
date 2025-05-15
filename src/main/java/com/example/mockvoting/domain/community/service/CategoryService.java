package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;

    /**
     * 게시글 카테고리 전체 조회
     */
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryMapper.selectAllCategories();
    }

    public List<CategoryResponseDTO> getCategoriesByIsActive() {
        return categoryMapper.selectCategoriesByIsActive();
    }

    /**
     * 카테고리별 게시글 수 조회
     */
    public int selectPostCountByCategory(@Param("categoryCode") String categoryCode) {
        return categoryMapper.selectPostCountByCategory(categoryCode);
    }

    /**
     * 카테고리 상태 업데이트 (활성화/비활성화)
     */
    public void updateCategoryStatus(Long categoryId, Boolean isActive) {
        categoryMapper.updateCategoryStatus(categoryId, isActive);
    }

    /**
     * 새 카테고리 추가
     */
    public void addCategory(CategoryResponseDTO categoryDTO) {
        categoryMapper.insertCategory(categoryDTO);
    }


    public void deleteCategory(Long categoryId) {
        categoryMapper.deleteCategory(categoryId);
    }

}
