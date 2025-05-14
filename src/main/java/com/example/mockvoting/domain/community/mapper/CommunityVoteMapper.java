package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.entity.CommunityVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommunityVoteMapper {

    // 사용자 - 타겟 투표 여부 조회
    Byte selectVoteByVoterAndTarget(@Param("voterId") String voterId,
                                    @Param("targetType") CommunityVote.TargetType targetType,
                                    @Param("targetId") Long targetId);
}
