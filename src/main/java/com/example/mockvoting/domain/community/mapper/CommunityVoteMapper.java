package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.CommunityVoteResultDTO;
import com.example.mockvoting.domain.community.entity.CommunityVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityVoteMapper {

    // 사용자 - 타겟 투표 여부 조회
    Byte selectVoteByVoterAndTarget(@Param("voterId") String voterId,
                                    @Param("targetType") CommunityVote.TargetType targetType,
                                    @Param("targetId") Long targetId);

    // 여러 개의 타겟에 대해 투표 정보 조회
    List<CommunityVoteResultDTO> selectVotesByVoterAndTargetIds(
            @Param("voterId") String voterId,
            @Param("targetType") CommunityVote.TargetType targetType,  // "POST_COMMENT"
            @Param("targetIds") List<Long> targetIds
    );
}
