package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.NoticeSummaryDTO;
import com.example.mockvoting.domain.community.dto.PopularPostResponseDTO;
import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    // 게시글 상세 조회
    PostDetailResponseDTO selectPostDetailById(@Param("id") Long id);

    // 활성화된 카테고리에 속한 게시글 전체 조회
    List<PostSummaryResponseDTO> selectPostsFromActiveCategories(@Param("offset") int offset, @Param("limit") int limit);

    // 카테고리별 게시글 조회
    List<PostSummaryResponseDTO> selectPostsByCategory(@Param("categoryCode") String categoryCode,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);
    // 인기 게시글 조회
    List<PopularPostResponseDTO> selectPopularPosts();

    // 최근 공지사항 조회
    List<NoticeSummaryDTO> selectRecentNotices();

    // 게시글 조회수 업데이트
    void updateViewCountById(@Param("id") Long id);

    // 게시글 id로 카테고리 id 조회
    Long selectCategoryIdById(@Param("id") Long id);

    // 게시글 id로 게시글 작성자 userId 조회
    String selectAuthorIdById(@Param("id") Long id);
}
