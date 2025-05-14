package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.PostAttachmentResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostAttachmentMapper {
    // 게시글별 첨부파일 목록 조회
    List<PostAttachmentResponseDTO> selectAttachmentsByPostId(@Param("postId") Long postId);
}
