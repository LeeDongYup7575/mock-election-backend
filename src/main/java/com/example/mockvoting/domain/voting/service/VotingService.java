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
import java.util.List;
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

        // 3. 사용자 지갑 확인
        Wallet wallet = walletMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("지갑을 찾을 수 없음: userId={}", userId);
                    return new CustomException("투표하려면 지갑 연결이 필요합니다.");
                });

        // 4. 지갑 타입에 따라 다르게 처리
        try {
            if ("METAMASK".equals(wallet.getWalletType())) {
                // 메타마스크 지갑은 프론트엔드에서 트랜잭션을 전송하므로
                // 백엔드에서는 블록체인 트랜잭션 확인 없이 투표 처리만 수행
                // 프론트엔드에서 submitVote 함수 호출 전 블록체인 트랜잭션을 완료해야 함
                log.info("메타마스크 지갑 사용자 투표 처리: userId={}", userId);

                // 토큰 잔액 확인 (블록체인에서 가져온 잔액)
                int balance = walletService.getTokenBalance(userId);
                if (balance < 1) {
                    log.error("토큰 잔액 부족: userId={}, balance={}", userId, balance);
                    throw new CustomException("투표에 필요한 토큰이 부족합니다.");
                }
            } else {
                // 내부 지갑은 서버에서 토큰 차감 처리
                log.info("내부 지갑 사용자 토큰 차감 시작: userId={}", userId);
                walletService.deductToken(userId, 1);
            }
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

        // 5. 후보자별 투표 통계가 존재하는지 확인
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

        // 6. 사용자의 투표 상태 업데이트 (user 테이블의 is_election 필드 사용)
        log.info("사용자 투표 상태 업데이트: userId={}, isElection=true", userId);
        userMapper.updateUserElectionStatus(userId, true);

        // 7. 백분율 원자적 업데이트 비동기 처리
        updateStatisticsAsync(sgId);

        // 8. 투표 결과 반환
        return getVotingStats(sgId);
    }

    /**
     * 메타마스크 투표 검증 및 제출
     * 프론트엔드에서 블록체인 트랜잭션 완료 후 호출
     */
    // VotingService.java의 verifyAndSubmitMetaMaskVoting 메소드 개선
    @Transactional
    public VotingStatsDTO verifyAndSubmitMetaMaskVoting(String sgId, Integer candidateId, String userId, String transactionHash) {
        // 1. 사용자 조회
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new CustomException("사용자를 찾을 수 없습니다.");
                });

        // 2. 이미 투표했는지 확인
        if (user.isElection()) {
            log.warn("이미 투표한 사용자: userId={}", userId);
            throw new CustomException("이미 투표에 참여하셨습니다.");
        }

        // 3. 지갑 타입 확인
        Wallet wallet = walletMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("지갑을 찾을 수 없음: userId={}", userId);
                    return new CustomException("투표하려면 지갑 연결이 필요합니다.");
                });

        if (!"METAMASK".equals(wallet.getWalletType())) {
            log.error("메타마스크 지갑이 아님: userId={}, walletType={}", userId, wallet.getWalletType());
            throw new CustomException("메타마스크 지갑 투표만 이 API를 사용할 수 있습니다.");
        }

        // 4. 블록체인 트랜잭션 검증 - 개선된 검증 로직
        boolean isValidTransaction = walletService.verifyTransaction(transactionHash, wallet.getWalletAddress(), candidateId);

        if (!isValidTransaction) {
            log.error("유효하지 않은 블록체인 트랜잭션: userId={}, txHash={}", userId, transactionHash);
            throw new CustomException("유효하지 않은 투표 트랜잭션입니다. 트랜잭션이 블록체인에 확인되지 않았거나 올바른 투표 트랜잭션이 아닙니다.");
        }

        log.info("메타마스크 투표 트랜잭션 검증 성공: userId={}, transactionHash={}", userId, transactionHash);

        // 5. 후보자별 투표 통계가 존재하는지 확인
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

        // 6. 사용자의 투표 상태 업데이트
        log.info("사용자 투표 상태 업데이트: userId={}, isElection=true", userId);
        userMapper.updateUserElectionStatus(userId, true);

        // 7. 토큰 잔액 새로고침 시도 - 블록체인에서 실제 잔액 조회 후 DB 업데이트
        try {
            BigInteger currentBalance = walletService.getTokenBalanceFromBlockchain(wallet.getWalletAddress());
            int tokenBalance = currentBalance.divide(BigInteger.TEN.pow(18)).intValue();

            // DB에 저장된 토큰 잔액 업데이트
            walletMapper.updateTokenBalance(userId, tokenBalance);
            log.info("투표 후 토큰 잔액 업데이트: userId={}, tokenBalance={}", userId, tokenBalance);
        } catch (Exception e) {
            log.warn("투표 후 토큰 잔액 조회 실패: {}", e.getMessage());
        }

        // 8. 백분율 원자적 업데이트 비동기 처리
        updateStatisticsAsync(sgId);

        // 9. 투표 결과 반환
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