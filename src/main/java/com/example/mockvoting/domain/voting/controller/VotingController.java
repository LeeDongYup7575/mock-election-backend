package com.example.mockvoting.domain.voting.controller;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.dto.VotingRequestDTO;
import com.example.mockvoting.domain.voting.dto.VotingStatsDTO;
import com.example.mockvoting.domain.voting.dto.MetaMaskVotingDTO;
import com.example.mockvoting.domain.voting.service.VotingService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/votings")
public class VotingController {

    private final VotingService votingService;

    /**
     * 투표 정보 조회
     */
    @GetMapping("/{sgId}")
    public ResponseEntity<ApiResponse<VotingCardDTO>> getElectionById(@PathVariable String sgId) {
        VotingCardDTO election = votingService.getElectionById(sgId);

        if (election == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(election));
    }

    /**
     * 투표의 모든 정당 정책 조회
     */
    @GetMapping("/{sgId}/party-policies")
    public ResponseEntity<ApiResponse<List<PartyPolicyDTO>>> getPartyPoliciesByElectionId(@PathVariable String sgId) {
        List<PartyPolicyDTO> policies = votingService.getPartyPoliciesBySgId(sgId);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    /**
     * 투표의 모든 정당 이름 조회
     */
    @GetMapping("/{sgId}/party-names")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctPartyNamesByElectionId(@PathVariable String sgId) {
        List<String> partyNames = votingService.getDistinctPartyNamesBySgId(sgId);
        return ResponseEntity.ok(ApiResponse.success(partyNames));
    }

    /**
     * 투표 제출 API - 내부 지갑 사용자용
     */
    @PostMapping("/{sgId}/vote")
    public ResponseEntity<ApiResponse<VotingStatsDTO>> submitVoting(
            @PathVariable String sgId,
            @RequestBody VotingRequestDTO votingRequest,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        VotingStatsDTO result = votingService.submitVoting(sgId, votingRequest.getCandidateId(), userId);

        return ResponseEntity.ok(ApiResponse.success("투표가 성공적으로 제출되었습니다.", result));
    }

    /**
     * 메타마스크 지갑 투표 제출 API - 블록체인 트랜잭션 해시 포함
     */
    @PostMapping("/{sgId}/vote/metamask")
    public ResponseEntity<ApiResponse<VotingStatsDTO>> submitMetaMaskVoting(
            @PathVariable String sgId,
            @RequestBody MetaMaskVotingDTO votingRequest,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        log.info("메타마스크 투표 요청: userId={}, sgId={}, candidateId={}, txHash={}",
                userId, sgId, votingRequest.getCandidateId(), votingRequest.getTransactionHash());

        VotingStatsDTO result = votingService.verifyAndSubmitMetaMaskVoting(
                sgId,
                votingRequest.getCandidateId(),
                userId,
                votingRequest.getTransactionHash()
        );

        return ResponseEntity.ok(ApiResponse.success("메타마스크 투표가 성공적으로 제출되었습니다.", result));
    }

    /**
     * 투표 통계 조회 API
     */
    @GetMapping("/{sgId}/stats")
    public ResponseEntity<ApiResponse<VotingStatsDTO>> getVotingStats(@PathVariable String sgId) {
        VotingStatsDTO stats = votingService.getVotingStats(sgId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 사용자 투표 상태 확인 API
     */
    @GetMapping("/{sgId}/status")
    public ResponseEntity<ApiResponse<Boolean>> checkVotingStatus(
            @PathVariable String sgId,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("투표 상태 확인 요청: 사용자={}, 선거ID={}", userId, sgId);

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다."));
            }

            boolean hasVoted = votingService.hasUserVoted(userId, sgId);
            log.info("투표 상태 확인 결과: 사용자={}, 선거ID={}, 투표여부={}", userId, sgId, hasVoted);

            return ResponseEntity.ok(ApiResponse.success(hasVoted));
        } catch (Exception e) {
            log.error("투표 상태 확인 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("투표 상태 확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 투표 가능 여부 확인 API
     * - 지갑 연결, 토큰 잔액, 이전 투표 여부 등 확인
     */
    @GetMapping("/{sgId}/eligibility")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkVotingEligibility(
            @PathVariable String sgId,
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("투표 가능 여부 확인 요청: 사용자={}, 선거ID={}", userId, sgId);

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다."));
            }

            boolean canVote = votingService.canUserVote(userId, sgId);
            boolean hasVoted = votingService.hasUserVoted(userId, sgId);

            Map<String, Object> result = Map.of(
                    "canVote", canVote,
                    "hasVoted", hasVoted
            );

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("투표 가능 여부 확인 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("투표 가능 여부 확인 중 오류가 발생했습니다."));
        }
    }
}