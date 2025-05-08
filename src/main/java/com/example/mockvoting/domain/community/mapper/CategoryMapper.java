package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    // 게시글 카테고리 전체 조회
    List<CategoryResponseDTO> selectAllCategories();

    // 카테고리별 게시글 개수 조회
    int selectPostCountByCategory(@Param("categoryCode") String categoryCode);
}
