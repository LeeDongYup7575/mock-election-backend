package com.example.mockvoting.domain.policyQuestion.controller;

import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionDTO;
import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionOptionsDTO;
import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionSelectDTO;
import com.example.mockvoting.domain.policyQuestion.services.PolicyQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/policyQuestion")
@RequiredArgsConstructor
@Slf4j
public class PolicyQuestionController {

    private final PolicyQuestionService policyQuestionService;

    /**
     * 최신 정책 질문을 조회합니다.
     * @return 최신 정책 질문 및 옵션 정보
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestQuestion() {
        try {
            log.info("Getting latest policy question");

            // 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;

            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getPrincipal().equals("anonymousUser")) {
                userId = authentication.getName();
                log.info("Authenticated user: {} is viewing latest question", userId);
            }

            PolicyQuestionDTO question = policyQuestionService.getLatestPolicyQuestion();

            if (question == null) {
                log.info("No policy question available");
                return ResponseEntity.noContent().build();
            }

            // 인증된 사용자라면 사용자 선택 정보 추가
            if (userId != null) {
                PolicyQuestionSelectDTO userSelection = policyQuestionService.getUserSelection(question.getId(), userId);
                if (userSelection != null) {
                    question.setUserSelectedOptionId(userSelection.getSelectOptionId());
                    log.info("User {} has selected option {}", userId, userSelection.getSelectOptionId());
                }
            }

            log.info("Returning latest policy question with id: {}", question.getId());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            log.error("Error getting latest policy question", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 특정 ID의 정책 질문을 조회합니다.
     * @param id 정책 질문 ID
     * @return 정책 질문 및 옵션 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable int id) {
        try {
            log.info("Getting policy question with id: {}", id);
            PolicyQuestionDTO question = policyQuestionService.getPolicyQuestionById(id);

            if (question == null) {
                log.info("Policy question with id {} not found", id);
                return ResponseEntity.notFound().build();
            }

            log.info("Returning policy question with id: {}", question.getId());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            log.error("Error getting policy question with id: {}", id, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 사용자의 현재 선택을 조회합니다.
     * @param questionId 질문 ID
     * @return 사용자 선택 정보
     */
    @GetMapping("/user-selection/{questionId}")
    public ResponseEntity<?> getUserSelection(@PathVariable int questionId) {
        try {
            log.info("Getting user selection for question id: {}", questionId);

            // 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getPrincipal().equals("anonymousUser")) {
                log.warn("Unauthorized access to user selection");
                return ResponseEntity.status(401).body("Authentication required");
            }

            String userId = authentication.getName();

            PolicyQuestionSelectDTO userSelection = policyQuestionService.getUserSelection(questionId, userId);
            if (userSelection == null) {
                log.info("No selection found for user {} and question {}", userId, questionId);
                return ResponseEntity.noContent().build();
            }

            log.info("Found selection: optionId={} for user {} and question {}",
                    userSelection.getSelectOptionId(), userId, questionId);
            return ResponseEntity.ok(userSelection);
        } catch (Exception e) {
            log.error("Error getting user selection", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 정책 질문 옵션에 투표합니다.
     * @param payload 요청 페이로드 (optionId, questionId, 선택적으로 previousOptionId 포함)
     * @return 업데이트된 투표 결과
     */
    @PostMapping("/vote")
    public ResponseEntity<?> vote(@RequestBody Map<String, Object> payload) {
        try {
            // 필수 파라미터 검증
            if (!payload.containsKey("optionId")) {
                log.warn("Vote request missing optionId");
                return ResponseEntity.badRequest().body("optionId is required");
            }

            int optionId = ((Number) payload.get("optionId")).intValue();
            Integer questionId = payload.containsKey("questionId") ?
                    ((Number) payload.get("questionId")).intValue() : null;

            // 이전 선택 옵션 ID (있는 경우)
            Integer previousOptionId = payload.containsKey("previousOptionId") ?
                    ((Number) payload.get("previousOptionId")).intValue() : null;

            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;

            // 인증된 사용자인 경우 ID 가져오기
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getPrincipal().equals("anonymousUser")) {
                userId = authentication.getName(); // 사용자 ID 가져오기
                log.info("Authenticated user: {} is voting", userId);
            } else {
                log.info("Anonymous user is voting");
            }

            PolicyQuestionDTO updatedQuestion;

            // 사용자 ID가 있으면 사용자 선택 저장 메서드 호출
            if (userId != null && questionId != null) {
                // 이전 선택이 있는 경우 (투표 변경)
                if (previousOptionId != null) {
                    log.info("User {} is changing vote from option {} to option {}",
                            userId, previousOptionId, optionId);
                    updatedQuestion = policyQuestionService.saveUserSelection(
                            questionId, optionId, userId, previousOptionId);
                } else {
                    // 새 투표
                    updatedQuestion = policyQuestionService.saveUserSelection(
                            questionId, optionId, userId);
                }
            } else if (questionId != null) {
                // 익명 사용자는 기존 메서드 사용
                updatedQuestion = policyQuestionService.vote(optionId);
            } else {
                // questionId가 없는 경우
                PolicyQuestionOptionsDTO option = policyQuestionService.findOptionById(optionId);
                if (option == null) {
                    throw new IllegalArgumentException("Invalid option ID: " + optionId);
                }
                questionId = option.getQuestionId();

                if (userId != null) {
                    updatedQuestion = policyQuestionService.saveUserSelection(
                            questionId, optionId, userId);
                } else {
                    updatedQuestion = policyQuestionService.vote(optionId);
                }
            }

            return ResponseEntity.ok(updatedQuestion);
        } catch (IllegalArgumentException e) {
            log.error("Vote failed due to invalid argument", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Vote failed due to internal error", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}