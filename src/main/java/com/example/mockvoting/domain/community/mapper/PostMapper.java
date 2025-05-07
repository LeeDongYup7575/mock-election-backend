package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
    // 게시글 상세 조회
    PostDetailResponseDTO selectPostDetailById(@Param("id") Integer id);
}
