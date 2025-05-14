package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.PostCommentResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {
    // 최상위 댓글 조회
    List<PostCommentResponseDTO> selectTopLevelCommentsByPostId(
            @Param("postId") Long postId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
