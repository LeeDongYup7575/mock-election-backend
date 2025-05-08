package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    // 게시글 상세 조회
    PostDetailResponseDTO selectPostDetailById(@Param("id") Integer id);

    // 카테고리별 게시글 조회
    List<PostSummaryResponseDTO> selectPostsByCategory(@Param("categoryCode") String categoryCode,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);
}
