package com.example.mockvoting.domain.spamcheck.mapper;

import com.example.mockvoting.domain.spamcheck.dto.PostContentCheckDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SpamCheckMapper {
    // 게시글: 최근 1분/10분 내 글 목록
    List<PostContentCheckDTO> selectRecentPosts(@Param("userId") String userId,
                                                @Param("since") LocalDateTime since);
    // 댓글: 최근 1분/10분 내 댓글 목록
    List<String> selectRecentComments(@Param("userId") String userId,
                                      @Param("since") LocalDateTime since);
}
