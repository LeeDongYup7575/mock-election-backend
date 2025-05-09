package com.example.mockvoting.domain.quiz.services;

import com.example.mockvoting.domain.quiz.dto.QuizDTO;
import com.example.mockvoting.domain.quiz.dto.QuizOptionDTO;
import com.example.mockvoting.domain.quiz.mappers.VoteQuizMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizService {

    private final VoteQuizMapper voteQuizMapper;

    /**
     * ID로 퀴즈 조회
     */
    public Optional<QuizDTO> getQuizById(Long id) {
        log.debug("ID로 퀴즈 조회: {}", id);
        QuizDTO quiz = voteQuizMapper.findQuizById(id);

        if (quiz != null) {
            loadQuizOptions(quiz);
        }

        return Optional.ofNullable(quiz);
    }

    /**
     * 랜덤 퀴즈 조회
     */
    public QuizDTO getRandomQuiz() {
        log.debug("랜덤 퀴즈 조회");
        QuizDTO quiz = voteQuizMapper.findRandomQuiz();

        if (quiz != null) {
            loadQuizOptions(quiz);
        }

        return quiz;
    }

    /**
     * 다음 퀴즈 조회
     */
    public Optional<QuizDTO> getNextQuiz(Long currentId) {
        log.debug("다음 퀴즈 조회. 현재 ID: {}", currentId);

        Long nextId = voteQuizMapper.findNextQuizId(currentId);

        // 다음 퀴즈가 없으면 첫 번째 퀴즈로 돌아감
        if (nextId == null) {
            nextId = voteQuizMapper.findFirstQuizId();
            log.debug("마지막 퀴즈 이후 첫 번째 퀴즈로 돌아감: {}", nextId);
        }

        return getQuizById(nextId);
    }

    /**
     * 이전 퀴즈 조회
     */
    public Optional<QuizDTO> getPreviousQuiz(Long currentId) {
        log.debug("이전 퀴즈 조회. 현재 ID: {}", currentId);

        List<Long> allIds = voteQuizMapper.findAllQuizIds();

        if (allIds.isEmpty()) {
            return Optional.empty();
        }

        int currentIndex = -1;
        for (int i = 0; i < allIds.size(); i++) {
            if (allIds.get(i).equals(currentId)) {
                currentIndex = i;
                break;
            }
        }

        Long prevId;
        if (currentIndex <= 0) {
            // 첫 번째 퀴즈이거나 찾지 못한 경우 마지막 퀴즈로
            prevId = voteQuizMapper.findLastQuizId();
            log.debug("첫 번째 퀴즈 이전 마지막 퀴즈로: {}", prevId);
        } else {
            prevId = allIds.get(currentIndex - 1);
            log.debug("이전 퀴즈 ID: {}", prevId);
        }

        return getQuizById(prevId);
    }

    /**
     * 첫 번째 퀴즈 조회
     */
    public Optional<QuizDTO> getFirstQuiz() {
        log.debug("첫 번째 퀴즈 조회");
        Long firstId = voteQuizMapper.findFirstQuizId();
        if (firstId == null) {
            return Optional.empty();
        }
        return getQuizById(firstId);
    }

    /**
     * 모든 퀴즈 조회
     */
    public List<QuizDTO> getAllQuizzes() {
        log.debug("모든 퀴즈 조회");
        List<QuizDTO> quizzes = voteQuizMapper.findAllQuizzes();

        // 각 퀴즈의 옵션 로드
        quizzes.forEach(this::loadQuizOptions);

        return quizzes;
    }

    /**
     * 퀴즈에 옵션과 정답 로드
     */
    private void loadQuizOptions(QuizDTO quiz) {
        // 퀴즈 옵션 로드
        List<QuizOptionDTO> options = voteQuizMapper.findOptionsByQuizId(quiz.getId());
        options.forEach(quiz::addOption);

        // 정답 옵션 찾아서 correctAnswer 설정
        QuizOptionDTO correctOption = voteQuizMapper.findCorrectOptionByQuizId(quiz.getId());
        if (correctOption != null) {
            // 여기서 옵션 번호를 직접 사용 (ID 대신)
            quiz.setCorrectAnswer(correctOption.getOptionNumber());
        }
    }
}