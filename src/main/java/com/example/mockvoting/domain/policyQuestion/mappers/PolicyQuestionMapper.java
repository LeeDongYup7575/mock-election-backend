package com.example.mockvoting.domain.policyQuestion.mappers;

import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionDTO;
import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionOptionsDTO;
import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionSelectDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PolicyQuestionMapper {
    // 모든 정책 질문 목록 조회
    List<PolicyQuestionDTO> findAllPolicyQuestions();

    // 특정 ID의 정책 질문 조회
    PolicyQuestionDTO findPolicyQuestionById(@Param("id") int id);

    // 특정 정책 질문의 옵션 목록 조회
    List<PolicyQuestionOptionsDTO> findOptionsByQuestionId(@Param("questionId") int questionId);

    // 투표 카운트 증가
    int incrementOptionCount(@Param("optionId") int optionId);

    // 투표 카운트 감소 (새로 추가)
    int decrementOptionCount(@Param("optionId") int optionId);

    // 특정 옵션 조회
    PolicyQuestionOptionsDTO findOptionById(@Param("optionId") int optionId);

    // 사용자 옵션 선택 저장
    int updateSelectOption(@Param("questionId") int questionId, @Param("selectOptionId") int selectOptionId, @Param("userId") String userId);

    // 사용자의 현재 선택 옵션 조회
    PolicyQuestionSelectDTO findUserSelection(@Param("userId") String userId, @Param("questionId") int questionId);

    // 사용자 선택 삭제 (새로 추가)
    int deleteUserSelection(@Param("userId") String userId, @Param("questionId") int questionId);
}