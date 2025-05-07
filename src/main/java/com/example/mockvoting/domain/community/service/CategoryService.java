package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;

    /**
     *  게시글 카테고리 전체 조회
     */
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryMapper.selectAllCategories();
    }

    /**
     *  카테고리별 게시글 조회
     */
    public Page<PostSummaryResponseDTO> getPostsByCategory(String categoryCode, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = (int) pageable.getPageSize();

        List<PostSummaryResponseDTO> posts = categoryMapper.selectPostsByCategory(categoryCode, offset, limit);
        int total = categoryMapper.selectPostCountByCategory(categoryCode);

        return new PageImpl<>(posts, pageable, total);
    }
}
