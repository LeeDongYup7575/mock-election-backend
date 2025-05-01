package com.example.mockvoting.domain.voting.mapper;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.entity.VotingStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VotingMapper {

    // 특정 투표 정보 조회
    VotingCardDTO getElectionById(@Param("sgId") String sgId);

    // 특정 투표의 모든 정당 정책 조회
    List<PartyPolicyDTO> getPartyPoliciesBySgId(@Param("sgId") String sgId);

    // 특정 투표의 모든 정당 이름 조회
    List<String> getDistinctPartyNamesBySgId(@Param("sgId") String sgId);

    // 특정 투표의 총 투표 수 조회
    int countBySgId(@Param("sgId") String sgId);

    // 특정 투표의 후보자별 투표 수 조회
    List<VotingStats> getVotingStatsBySgId(@Param("sgId") String sgId);

    // 특정 후보의 투표 수 원자적 증가 (동시성 문제 해결)
    void incrementVoteCount(@Param("sgId") String sgId, @Param("candidateId") Integer candidateId);

    // 특정 후보의 투표 통계 조회
    VotingStats getVotingStatsByCandidateId(@Param("sgId") String sgId, @Param("candidateId") Integer candidateId);

    // 새로운 투표 통계 삽입
    void insertVotingStats(VotingStats votingStats);

    // 모든 후보의 백분율 원자적 업데이트
    void updateAllPercentages(@Param("sgId") String sgId);

    // 활성화된 모든 투표 ID 조회 (스케줄링용)
    List<String> findAllActiveVotingIds();
}