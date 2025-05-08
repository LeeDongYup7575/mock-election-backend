package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
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

}
