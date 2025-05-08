package com.example.mockvoting.domain.quiz.controller;

import com.example.mockvoting.domain.quiz.dto.QuizDTO;
import com.example.mockvoting.domain.quiz.services.QuizService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    /**
     * 특정 ID의 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getQuizById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            return quizService.getQuizById(id)
                    .map(quiz -> ResponseEntity.ok(ApiResponse.success(quiz)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("퀴즈를 찾을 수 없습니다.")));
        } catch (Exception e) {
            log.error("퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 랜덤 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/random")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRandomQuiz(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            QuizDTO quiz = quizService.getRandomQuiz();
            if (quiz == null) {
                return ResponseEntity.ok(ApiResponse.error("퀴즈를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(quiz));
        } catch (Exception e) {
            log.error("랜덤 퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("랜덤 퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 첫 번째 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/first")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFirstQuiz(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            return quizService.getFirstQuiz()
                    .map(quiz -> ResponseEntity.ok(ApiResponse.success(quiz)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("퀴즈가 없습니다.")));
        } catch (Exception e) {
            log.error("첫 번째 퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 다음 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/next/{currentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNextQuiz(@PathVariable Long currentId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            return quizService.getNextQuiz(currentId)
                    .map(quiz -> ResponseEntity.ok(ApiResponse.success(quiz)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("다음 퀴즈를 찾을 수 없습니다.")));
        } catch (Exception e) {
            log.error("다음 퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 이전 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/previous/{currentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPreviousQuiz(@PathVariable Long currentId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            return quizService.getPreviousQuiz(currentId)
                    .map(quiz -> ResponseEntity.ok(ApiResponse.success(quiz)))
                    .orElse(ResponseEntity.ok(ApiResponse.error("이전 퀴즈를 찾을 수 없습니다.")));
        } catch (Exception e) {
            log.error("이전 퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 퀴즈 조회 - 인증된 사용자만 접근 가능
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllQuizzes(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");

            List<QuizDTO> quizzes = quizService.getAllQuizzes();
            if (quizzes.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("퀴즈가 없습니다."));
            }
            return ResponseEntity.ok(ApiResponse.success(quizzes));
        } catch (Exception e) {
            log.error("모든 퀴즈 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("퀴즈 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}