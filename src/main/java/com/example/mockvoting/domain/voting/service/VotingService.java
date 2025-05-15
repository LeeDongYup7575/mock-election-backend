package com.example.mockvoting.domain.voting.service;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.dto.VotingStatsDTO;
import com.example.mockvoting.domain.voting.entity.VotingStats;
import com.example.mockvoting.domain.voting.mapper.VotingMapper;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.domain.wallet.entity.Wallet;
import com.example.mockvoting.domain.wallet.mapper.WalletMapper;
import com.example.mockvoting.domain.wallet.service.WalletService;
import com.example.mockvoting.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotingService {

    private final VotingMapper votingMapper;
    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final WalletService walletService;

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.token-contract-address}")
    private String tokenContractAddress;

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

    @Transactional
    public VotingStatsDTO submitVoting(String sgId, Integer candidateId, String userId) {
        try {
            // 1. 사용자 조회
            User user = userMapper.findByUserId(userId)
                    .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

            // 2. 이미 투표했는지 확인
            if (user.isElection()) {
                log.warn("이미 투표한 사용자: userId={}", userId);
                throw new CustomException("이미 투표에 참여하셨습니다.");
            }

            // 3. 사용자 지갑 확인
            Wallet wallet = walletMapper.findByUserId(userId)
                    .orElseThrow(() -> new CustomException("투표하려면 지갑 연결이 필요합니다."));

            // 4. 토큰 차감
            if (wallet.getTokenBalance() < 1) {
                throw new CustomException("투표에 필요한 토큰이 부족합니다.");
            }

            int newBalance = wallet.getTokenBalance() - 1;
            walletMapper.updateTokenBalance(userId, newBalance);
            log.info("토큰 차감 완료: userId={}, 남은 토큰={}", userId, newBalance);

            // 5. 후보자별 투표 통계 처리 (기존 방식 유지)
            VotingStats stats = votingMapper.getVotingStatsByCandidateId(sgId, candidateId);

            if (stats == null) {
                log.info("새 투표 통계 생성: sgId={}, candidateId={}", sgId, candidateId);
                VotingStats newStats = VotingStats.builder()
                        .sgId(sgId)
                        .candidateId(candidateId)
                        .voteCount(1)
                        .percentage(0.0)
                        .build();
                votingMapper.insertVotingStats(newStats);
            } else {
                log.info("투표 수 증가: sgId={}, candidateId={}, 현재 count={}", sgId, candidateId, stats.getVoteCount());
                votingMapper.incrementVoteCount(sgId, candidateId);
            }

            // 6. 사용자의 투표 상태 업데이트
            userMapper.updateUserElectionStatus(userId, true);

            // 7. 백분율 업데이트
            votingMapper.updateAllPercentages(sgId);

            // 8. 투표 결과 반환
            return getVotingStats(sgId);

        } catch (Exception e) {
            log.error("투표 처리 중 오류 발생: userId={}, sgId={}, candidateId={}", userId, sgId, candidateId, e);
            throw e;
        }
    }

    /**
     * 메타마스크 투표 검증 및 제출 (candidateId로 처리)
     */
    @Transactional
    public VotingStatsDTO verifyAndSubmitMetaMaskVoting(String sgId, Integer candidateId, String userId, String transactionHash) {
        // 1. 사용자 조회
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        // 2. 이미 투표했는지 확인
        if (user.isElection()) {
            throw new CustomException("이미 투표에 참여하셨습니다.");
        }

        // 3. 지갑 타입 확인
        Wallet wallet = walletMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("투표하려면 지갑 연결이 필요합니다."));

        if (!"METAMASK".equals(wallet.getWalletType())) {
            throw new CustomException("메타마스크 지갑 투표만 이 API를 사용할 수 있습니다.");
        }

        // 4. 블록체인 트랜잭션 검증 또는 DB 토큰 차감
        if (transactionHash != null && !transactionHash.isEmpty() && !transactionHash.equals("INTERNAL")) {
            // 실제 블록체인 트랜잭션이 있는 경우 검증
            boolean isValidTransaction = walletService.verifyTransaction(
                    transactionHash, wallet.getWalletAddress(), candidateId);

            if (!isValidTransaction) {
                throw new CustomException("유효하지 않은 투표 트랜잭션입니다.");
            }
        } else {
            // DB 토큰 차감
            if (wallet.getTokenBalance() < 1) {
                throw new CustomException("투표에 필요한 토큰이 부족합니다.");
            }

            int newBalance = wallet.getTokenBalance() - 1;
            walletMapper.updateTokenBalance(userId, newBalance);
            log.info("DB 토큰 차감 완료: userId={}, 남은 토큰={}", userId, newBalance);
        }

        // 5. 후보자별 투표 통계 처리 (일반 투표와 동일)
        VotingStats stats = votingMapper.getVotingStatsByCandidateId(sgId, candidateId);

        if (stats == null) {
            log.info("새 투표 통계 생성: sgId={}, candidateId={}", sgId, candidateId);
            VotingStats newStats = VotingStats.builder()
                    .sgId(sgId)
                    .candidateId(candidateId)
                    .voteCount(1)
                    .percentage(0.0)
                    .build();
            votingMapper.insertVotingStats(newStats);
        } else {
            log.info("투표 수 증가: sgId={}, candidateId={}, 현재 count={}", sgId, candidateId, stats.getVoteCount());
            votingMapper.incrementVoteCount(sgId, candidateId);
        }

        // 6. 사용자의 투표 상태 업데이트
        userMapper.updateUserElectionStatus(userId, true);

        // 7. 토큰 잔액 새로고침 시도
        if (transactionHash != null && !transactionHash.isEmpty() && !transactionHash.equals("INTERNAL")) {
            try {
                BigInteger currentBalance = walletService.getTokenBalanceFromBlockchain(wallet.getWalletAddress());
                int tokenBalance = currentBalance.divide(BigInteger.TEN.pow(18)).intValue();
                walletMapper.updateTokenBalance(userId, tokenBalance);
                log.info("투표 후 토큰 잔액 업데이트: userId={}, tokenBalance={}", userId, tokenBalance);
            } catch (Exception e) {
                log.warn("투표 후 토큰 잔액 조회 실패: {}", e.getMessage());
            }
        }

        // 8. 백분율 업데이트
        votingMapper.updateAllPercentages(sgId);

        // 9. 투표 결과 반환
        return getVotingStats(sgId);
    }

    @Transactional(readOnly = true)
    public VotingStatsDTO getVotingStats(String sgId) {
        // 후보자별 투표 통계 가져오기
        List<VotingStats> stats = votingMapper.getVotingStatsBySgId(sgId);

        // 전체 투표수
        int totalVotes = stats.stream().mapToInt(VotingStats::getVoteCount).sum();
        double participation = totalVotes > 0 ? Math.min(100.0, (totalVotes/17.0) * 100) : 0.0;

        // 후보별 투표 결과 (기존 방식 유지)
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
     * 사용자 투표 가능 여부 확인
     * - 지갑 연결, 토큰 잔액, 이전 투표 여부 등을 확인
     */
    @Transactional(readOnly = true)
    public boolean canUserVote(String userId, String sgId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("유효하지 않은 사용자 ID: {}", userId);
            return false;
        }

        // 1. 사용자 존재 여부 확인
        User user = userMapper.findByUserId(userId).orElse(null);
        if (user == null) {
            log.warn("사용자를 찾을 수 없음: userId={}", userId);
            return false;
        }

        // 2. 이미 투표했는지 확인
        if (user.isElection()) {
            log.info("사용자가 이미 투표함: userId={}", userId);
            return false;
        }

        // 3. 지갑 연결 여부 확인
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            log.info("지갑이 연결되지 않음: userId={}", userId);
            return false;
        }

        // 4. 토큰 잔액 확인
        Wallet wallet = walletOpt.get();

        // 지갑 타입에 따른 잔액 확인
        if ("METAMASK".equals(wallet.getWalletType())) {
            try {
                // 블록체인에서 실제 잔액 확인
                int balance = walletService.getTokenBalance(userId);
                if (balance < 1) {
                    log.info("메타마스크 지갑 토큰 부족: userId={}, balance={}", userId, balance);
                    return false;
                }
            } catch (Exception e) {
                log.error("메타마스크 지갑 잔액 확인 중 오류: {}", e.getMessage());
                return false;
            }
        } else {
            // 내부 지갑 잔액 확인
            if (wallet.getTokenBalance() < 1) {
                log.info("내부 지갑 토큰 부족: userId={}, balance={}", userId, wallet.getTokenBalance());
                return false;
            }
        }

        log.info("사용자 투표 가능: userId={}, walletType={}", userId, wallet.getWalletType());
        return true;
    }

    /**
     * 모든 투표 통계 정기적으로 재계산 (동시성 문제 보정)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void scheduleStatisticsRecalculation() {
        try {
            // 모든 활성 투표 ID 조회
//            List<String> activeVotingIds = votingMapper.findAllActiveVotingIds();
//            log.info("정기 투표 통계 재계산 시작: 활성 투표 수={}", activeVotingIds.size());

            // 각 투표에 대해 통계 재계산
//            for (String sgId : activeVotingIds) {
//                updateStatistics(sgId);
//            }

            log.info("정기 투표 통계 재계산 완료");
        } catch (Exception e) {
            log.error("정기 통계 재계산 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    // Web3j 관련 유틸리티 메서드
    private Web3j getWeb3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    private ContractGasProvider getGasProvider() {
        return new DefaultGasProvider();
    }

    // VotingToken 컨트랙트 래퍼 클래스 (예시)
    public static class VotingToken {
        private final String contractAddress;
        private final Web3j web3j;
        private final Credentials credentials;
        private final ContractGasProvider gasProvider;

        public static VotingToken load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
            return new VotingToken(contractAddress, web3j, credentials, gasProvider);
        }

        private VotingToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
            this.contractAddress = contractAddress;
            this.web3j = web3j;
            this.credentials = credentials;
            this.gasProvider = gasProvider;
        }
    }
}