package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.PostCommentResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostCommentMapper {
    // 최상위 댓글 조회
//    List<PostCommentResponseDTO> selectTopLevelCommentsByPostId(
//            @Param("postId") Long postId,
//            @Param("offset") int offset,
//            @Param("limit") int limit
//    );

    /**
     * 최상위 댓글(depth=0) 목록 조회 (페이징)
     * @param params A map containing "postId" (Long), "offset" (int), "limit" (int)
     * @return 페이징된 최상위 댓글 리스트
     */
    List<PostCommentResponseDTO> selectTopLevelComments(Map<String, Object> params);

    /**
     * 주어진 parentId 목록의 자식 댓글(depth>0) 조회
     * @param parentIds 상위 댓글 ID 리스트
     * @return 해당 부모 ID를 가진 모든 자식 댓글
     */
    List<PostCommentResponseDTO> selectCommentsByParentIds(List<Long> parentIds);
}
