package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {
    // 게시글 카테고리 전체 조회
    List<CategoryResponseDTO> selectAllCategories();

    // 카테고리별 게시글 조회
    List<PostSummaryResponseDTO> selectPostsByCategory(@Param("categoryCode") String categoryCode,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    // 카테고리별 게시글 개수 조회
    int selectPostCountByCategory(@Param("categoryCode") String categoryCode);
}
