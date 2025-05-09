package com.example.mockvoting.domain.voting.mapper;

import com.example.mockvoting.domain.voting.entity.VoteHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface VoteHistoryMapper {
    // 투표 내역 추가
    void insertVoteHistory(VoteHistory voteHistory);

    // 특정 사용자, 특정 선거의 투표 내역 조회
    Optional<VoteHistory> findByUserIdAndSgId(@Param("userId") String userId, @Param("sgId") String sgId);

    // 특정 사용자의 모든 투표 내역 조회
    List<VoteHistory> findAllByUserId(@Param("userId") String userId);

    // 특정 선거의 모든 투표 내역 조회 (익명화 처리 필요)
    List<VoteHistory> findAllBySgId(@Param("sgId") String sgId);

    // 특정 사용자의 투표 총 횟수 조회
    int countByUserId(@Param("userId") String userId);
}