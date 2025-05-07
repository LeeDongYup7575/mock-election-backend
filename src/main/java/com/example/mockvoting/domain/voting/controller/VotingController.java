package com.example.mockvoting.domain.voting.controller;

import com.example.mockvoting.domain.voting.dto.VotingCardDTO;
import com.example.mockvoting.domain.voting.dto.PartyPolicyDTO;
import com.example.mockvoting.domain.voting.dto.VotingRequestDTO;
import com.example.mockvoting.domain.voting.dto.VotingStatsDTO;
import com.example.mockvoting.domain.voting.service.VotingService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 투표 제출 API
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

        String userId = (String) request.getAttribute("userId");
        boolean hasVoted = votingService.hasUserVoted(userId, sgId);

        return ResponseEntity.ok(ApiResponse.success(hasVoted));
    }
}