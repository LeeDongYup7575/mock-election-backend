package com.example.mockvoting.domain.voting.service;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.dto.VotingStatsDTO;
import com.example.mockvoting.domain.voting.entity.VotingStats;
import com.example.mockvoting.domain.voting.mapper.VotingMapper;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotingService {

    private final VotingMapper votingMapper;
    private final UserMapper userMapper;

    /**
     * 특정 투표 정보 조회
     */
    public VotingCardDTO getElectionById(String sgId) {
        return votingMapper.getElectionById(sgId);
    }

    /**
     * 특정 투표의 모든 정당 정책 조회
     */
    public List<PartyPolicyDTO> getPartyPoliciesBySgId(String sgId) {
        return votingMapper.getPartyPoliciesBySgId(sgId);
    }

    /**
     * 특정 투표의 모든 정당 이름 조회
     */
    public List<String> getDistinctPartyNamesBySgId(String sgId) {
        return votingMapper.getDistinctPartyNamesBySgId(sgId);
    }

    /**
     * 사용자 투표 제출 및 통계 계산
     * - 원자적 업데이트를 사용하여 동시성 문제 해결
     */
    @Transactional
    public VotingStatsDTO submitVoting(String sgId, Integer candidateId, String userId) {
        // 이미 투표했는지 확인
        if (hasUserVoted(userId, sgId)) {
            throw new CustomException("이미 투표에 참여하셨습니다.");
        }

        // 후보자별 투표 통계가 존재하는지 확인
        VotingStats stats = votingMapper.getVotingStatsByCandidateId(sgId, candidateId);

        if (stats == null) {
            // 해당 후보 통계가 없으면 새로 생성
            VotingStats newStats = VotingStats.builder()
                    .sgId(sgId)
                    .candidateId(candidateId)
                    .voteCount(1)
                    .percentage(0.0) // 임시 값
                    .build();
            votingMapper.insertVotingStats(newStats);
        } else {
            // 원자적으로 투표 수 증가
            votingMapper.incrementVoteCount(sgId, candidateId);
        }

        // 사용자의 투표 상태 업데이트 - 익명 투표를 위해 어떤 후보에 투표했는지는 저장하지 않음
        userMapper.updateUserElectionStatus(userId, true);

        // 백분율 원자적 업데이트 비동기 처리
        updateStatisticsAsync(sgId);

        // 투표 결과 반환
        return getVotingStats(sgId);
    }

    /**
     * 통계 업데이트 비동기 처리
     */
    @Async
    public void updateStatisticsAsync(String sgId) {
        try {
            updateStatistics(sgId);
        } catch (Exception e) {
            log.error("통계 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 투표 통계 업데이트 - 별도 트랜잭션으로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatistics(String sgId) {
        // 원자적으로 모든 후보의 백분율 업데이트
        votingMapper.updateAllPercentages(sgId);
    }

    /**
     * 사용자의 투표 여부 확인
     * - 컨트롤러에서 호출하는 인터페이스에 맞추기 위해 sgId 매개변수 추가
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(String userId, String sgId) {
        // 현재 구현에서는 사용자별로 투표 여부만 관리하기 때문에 sgId는 사용하지 않음
        // 추후 선거별 투표 여부를 관리하려면 sgId도 사용하도록 로직 변경 필요
        return userMapper.findByUserId(userId)
                .map(User::isElection)
                .orElse(false);
    }

    /**
     * 특정 투표의 투표 통계 조회
     */
    @Transactional(readOnly = true)
    public VotingStatsDTO getVotingStats(String sgId) {
        List<VotingStats> stats = votingMapper.getVotingStatsBySgId(sgId);
//        int totalVotes = votingMapper.countBySgId(sgId);

        int totalVotes = 20;
        // 참여율 계산 (실제 구현에서는 전체 유권자 수로 나누어야 함)
        double participation = totalVotes > 0 ? Math.min(100.0, totalVotes * 2.5) : 0.0;

        List<VotingStatsDTO.CandidateVoteDTO> voteResults = stats.stream()
                .map(stat -> VotingStatsDTO.CandidateVoteDTO.builder()
                        .candidateId(stat.getCandidateId())
                        .percentage(stat.getPercentage())
                        .voteCount(stat.getVoteCount())
                        .build())
                .collect(Collectors.toList());

        return VotingStatsDTO.builder()
                .sgId(sgId)
                .participation(participation)
                .votes(voteResults)
                .build();
    }

    /**
     * 모든 투표 통계 정기적으로 재계산 (동시성 문제 보정)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void scheduleStatisticsRecalculation() {
        try {
            // 모든 활성 투표 ID 조회
            List<String> activeVotingIds = votingMapper.findAllActiveVotingIds();

            // 각 투표에 대해 통계 재계산
            for (String sgId : activeVotingIds) {
                updateStatistics(sgId);
            }
        } catch (Exception e) {
            log.error("정기 통계 재계산 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}