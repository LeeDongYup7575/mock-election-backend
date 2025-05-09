package com.example.mockvoting.domain.voting.service;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.dto.VotingStatsDTO;
import com.example.mockvoting.domain.voting.entity.VotingStats;
import com.example.mockvoting.domain.voting.mapper.VotingMapper;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.domain.wallet.service.WalletService;
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
    private final WalletService walletService;

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
     * 사용자 투표 제출 및 통계 계산 (토큰 차감 로직 추가)
     * 익명성 보장을 위해 vote_history 테이블 사용하지 않음
     * 대신 user 테이블의 is_election 필드를 활용
     */
    @Transactional
    public VotingStatsDTO submitVoting(String sgId, Integer candidateId, String userId) {
        // 1. 사용자 조회
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new CustomException("사용자를 찾을 수 없습니다.");
                });

        // 2. 이미 투표했는지 확인 (user 테이블의 is_election 필드로 확인)
        if (user.isElection()) {
            log.warn("이미 투표한 사용자: userId={}", userId);
            throw new CustomException("이미 투표에 참여하셨습니다.");
        }

        // 3. 토큰 차감 (1개)
        try {
            log.info("토큰 차감 시작: userId={}, amount=1", userId);
            walletService.deductToken(userId, 1);
        } catch (CustomException e) {
            if (e.getMessage().contains("토큰 잔액이 부족합니다")) {
                log.error("토큰 잔액 부족: userId={}", userId);
                throw new CustomException("투표를 위한 토큰이 부족합니다. 토큰을 충전하세요.");
            } else if (e.getMessage().contains("연결된 지갑이 없습니다")) {
                log.error("연결된 지갑 없음: userId={}", userId);
                throw new CustomException("투표하려면 지갑 연결이 필요합니다.");
            } else {
                throw e;
            }
        }

        // 4. 후보자별 투표 통계가 존재하는지 확인
        VotingStats stats = votingMapper.getVotingStatsByCandidateId(sgId, candidateId);

        if (stats == null) {
            // 해당 후보 통계가 없으면 새로 생성
            log.info("새 투표 통계 생성: sgId={}, candidateId={}", sgId, candidateId);
            VotingStats newStats = VotingStats.builder()
                    .sgId(sgId)
                    .candidateId(candidateId)
                    .voteCount(1)
                    .percentage(0.0) // 임시 값
                    .build();
            votingMapper.insertVotingStats(newStats);
        } else {
            // 원자적으로 투표 수 증가
            log.info("투표 수 증가: sgId={}, candidateId={}, 현재 count={}", sgId, candidateId, stats.getVoteCount());
            votingMapper.incrementVoteCount(sgId, candidateId);
        }

        // 5. 사용자의 투표 상태 업데이트 (user 테이블의 is_election 필드 사용)
        log.info("사용자 투표 상태 업데이트: userId={}, isElection=true", userId);
        userMapper.updateUserElectionStatus(userId, true);

        // 6. 백분율 원자적 업데이트 비동기 처리
        updateStatisticsAsync(sgId);

        // 7. 투표 결과 반환
        return getVotingStats(sgId);
    }

    /**
     * 사용자가 투표했는지 확인 (간소화: user 테이블의 is_election 필드만 확인)
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(String userId, String sgId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("유효하지 않은 사용자 ID: {}", userId);
            return false;
        }

        User user = userMapper.findByUserId(userId).orElse(null);
        if (user == null) {
            log.warn("사용자를 찾을 수 없음: userId={}", userId);
            return false;
        }

        // 중요: user.isElection 값 확인 및 로깅
        boolean hasVoted = user.isElection();
        log.info("사용자 투표 상태 확인: userId={}, isElection={}", userId, hasVoted);
        return hasVoted;
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
        log.info("투표 통계 업데이트 완료: sgId={}", sgId);
    }

    /**
     * 특정 투표의 투표 통계 조회
     */
    @Transactional(readOnly = true)
    public VotingStatsDTO getVotingStats(String sgId) {
        List<VotingStats> stats = votingMapper.getVotingStatsBySgId(sgId);
        int totalVotes = stats.stream().mapToInt(VotingStats::getVoteCount).sum();

        // 참여율 계산 (실제 구현에서는 전체 유권자 수로 나누어야 함)
        double participation = totalVotes > 0 ? Math.min(100.0, totalVotes * 2.5) : 0.0;

        List<VotingStatsDTO.CandidateVoteDTO> voteResults = stats.stream()
                .map(stat -> VotingStatsDTO.CandidateVoteDTO.builder()
                        .candidateId(stat.getCandidateId())
                        .percentage(stat.getPercentage())
                        .voteCount(stat.getVoteCount())
                        .build())
                .collect(Collectors.toList());

        VotingStatsDTO statsDTO = VotingStatsDTO.builder()
                .sgId(sgId)
                .participation(participation)
                .votes(voteResults)
                .build();

        log.info("투표 통계 조회 결과: sgId={}, 참여율={}, 투표수={}",
                sgId, participation, totalVotes);

        return statsDTO;
    }

    /**
     * 모든 투표 통계 정기적으로 재계산 (동시성 문제 보정)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void scheduleStatisticsRecalculation() {
        try {
            // 모든 활성 투표 ID 조회
            List<String> activeVotingIds = votingMapper.findAllActiveVotingIds();
            log.info("정기 투표 통계 재계산 시작: 활성 투표 수={}", activeVotingIds.size());

            // 각 투표에 대해 통계 재계산
            for (String sgId : activeVotingIds) {
                updateStatistics(sgId);
            }

            log.info("정기 투표 통계 재계산 완료");
        } catch (Exception e) {
            log.error("정기 통계 재계산 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}