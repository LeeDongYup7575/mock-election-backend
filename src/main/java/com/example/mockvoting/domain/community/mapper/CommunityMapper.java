package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.CommunityStatsDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommunityMapper {

    // 회원 수, 게시글 수, 댓글 수 조회 for HeroSection
    CommunityStatsDTO selectCommunityStats();
}
