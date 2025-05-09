package com.example.mockvoting.domain.quiz.mappers;

import com.example.mockvoting.domain.quiz.dto.QuizDTO;
import com.example.mockvoting.domain.quiz.dto.QuizOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteQuizMapper {

    // 퀴즈 ID로 퀴즈 조회
    QuizDTO findQuizById(@Param("id") Long id);

    // 랜덤 퀴즈 조회
    QuizDTO findRandomQuiz();

    // 모든 퀴즈 ID 조회
    List<Long> findAllQuizIds();

    // 현재 ID보다 큰 다음 퀴즈 ID 조회
    Long findNextQuizId(@Param("currentId") Long currentId);

    // 첫 번째 퀴즈 ID 조회
    Long findFirstQuizId();

    // 마지막 퀴즈 ID 조회
    Long findLastQuizId();

    // 모든 퀴즈 조회
    List<QuizDTO> findAllQuizzes();

    // 퀴즈 ID로 모든 옵션 조회
    List<QuizOptionDTO> findOptionsByQuizId(@Param("quizId") Long quizId);

    // 퀴즈 ID로 정답 옵션 조회
    QuizOptionDTO findCorrectOptionByQuizId(@Param("quizId") Long quizId);
}