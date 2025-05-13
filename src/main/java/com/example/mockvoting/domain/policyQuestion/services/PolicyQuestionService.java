package com.example.mockvoting.domain.policyQuestion.services;

import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionDTO;
import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionOptionsDTO;

import com.example.mockvoting.domain.policyQuestion.dto.PolicyQuestionSelectDTO;
import com.example.mockvoting.domain.policyQuestion.mappers.PolicyQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyQuestionService {

    private final PolicyQuestionMapper policyQuestionMapper;

    /**
     * 최신 정책 질문을 조회합니다.
     * @return PolicyQuestionDTO
     */
    public PolicyQuestionDTO getLatestPolicyQuestion() {
        List<PolicyQuestionDTO> questions = policyQuestionMapper.findAllPolicyQuestions();
        if (questions.isEmpty()) {
            return null;
        }

        PolicyQuestionDTO latestQuestion = questions.get(0);
        List<PolicyQuestionOptionsDTO> options = policyQuestionMapper.findOptionsByQuestionId(latestQuestion.getId());

        // 백분율 계산
        calculatePercentages(options);

        latestQuestion.setOptions(options);
        return latestQuestion;
    }

    /**
     * 특정 ID의 정책 질문을 조회합니다.
     * @param id 정책 질문 ID
     * @return PolicyQuestionDTO
     */
    public PolicyQuestionDTO getPolicyQuestionById(int id) {
        PolicyQuestionDTO question = policyQuestionMapper.findPolicyQuestionById(id);
        if (question == null) {
            return null;
        }

        List<PolicyQuestionOptionsDTO> options = policyQuestionMapper.findOptionsByQuestionId(id);

        // 백분율 계산
        calculatePercentages(options);

        question.setOptions(options);
        return question;
    }

    /**
     * 정책 질문 옵션에 투표합니다.
     * @param optionId 옵션 ID
     * @return 업데이트된 정책 질문 DTO
     */
    @Transactional
    public PolicyQuestionDTO vote(int optionId) {
        // 옵션 정보 가져오기
        PolicyQuestionOptionsDTO option = policyQuestionMapper.findOptionById(optionId);
        if (option == null) {
            throw new IllegalArgumentException("Invalid option ID: " + optionId);
        }

        // 투표 카운트 증가
        policyQuestionMapper.incrementOptionCount(optionId);

        // 해당 정책 질문의 최신 정보 반환
        return getPolicyQuestionById(option.getQuestionId());
    }


    /**
     * 사용자의 옵션 선택을 저장합니다.
     * @param questionId 질문 ID
     * @param optionId 선택한 옵션 ID
     * @param userId 사용자 ID
     * @return 업데이트된 정책 질문 DTO
     */
    @Transactional
    public PolicyQuestionDTO saveUserSelection(int questionId, int optionId, String userId) {
        return saveUserSelection(questionId, optionId, userId, null);
    }

    /**
     * 사용자의 옵션 선택을 저장하고, 이전 선택이 있다면 해당 옵션의 카운트를 감소시킵니다.
     * @param questionId 질문 ID
     * @param optionId 선택한 옵션 ID
     * @param userId 사용자 ID
     * @param previousOptionId 이전에 선택한 옵션 ID (null이면 새 투표로 간주)
     * @return 업데이트된 정책 질문 DTO
     */
    @Transactional
    public PolicyQuestionDTO saveUserSelection(int questionId, int optionId, String userId, Integer previousOptionId) {
        log.info("Saving user selection: questionId={}, optionId={}, userId={}, previousOptionId={}",
                questionId, optionId, userId, previousOptionId);

        // 유효성 검사: 질문 ID 확인
        PolicyQuestionDTO question = policyQuestionMapper.findPolicyQuestionById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Invalid question ID: " + questionId);
        }

        // 유효성 검사: 옵션 ID 확인
        PolicyQuestionOptionsDTO option = policyQuestionMapper.findOptionById(optionId);
        if (option == null || option.getQuestionId() != questionId) {
            throw new IllegalArgumentException("Invalid option ID: " + optionId + " for question ID: " + questionId);
        }

        // 이전 선택 옵션이 명시적으로 제공되지 않은 경우, DB에서 조회
        if (previousOptionId == null) {
            PolicyQuestionSelectDTO currentSelection = policyQuestionMapper.findUserSelection(userId, questionId);
            if (currentSelection != null) {
                previousOptionId = currentSelection.getSelectOptionId();
                log.info("Found previous selection: {}", previousOptionId);
            }
        }

        // 이전 선택과 새 선택이 같으면 변경 없음
        if (previousOptionId != null && previousOptionId.equals(optionId)) {
            log.info("User selected the same option, no change needed");
            return getPolicyQuestionById(questionId);
        }

        // 이전 선택이 있으면 카운트 감소
        if (previousOptionId != null) {
            log.info("Decrementing count for previous option: {}", previousOptionId);
            policyQuestionMapper.decrementOptionCount(previousOptionId);
        }

        // 사용자 선택 저장
        policyQuestionMapper.updateSelectOption(questionId, optionId, userId);
        log.info("User selection updated");

        // 새 선택 카운트 증가
        policyQuestionMapper.incrementOptionCount(optionId);
        log.info("New option count incremented");

        // 업데이트된 질문 정보 반환
        return getPolicyQuestionById(questionId);
    }

    /**
     * 사용자의 현재 선택 옵션을 조회합니다.
     * @param questionId 질문 ID
     * @param userId 사용자 ID
     * @return PolicyQuestionSelectDTO
     */
    public PolicyQuestionSelectDTO getUserSelection(int questionId, String userId) {
        return policyQuestionMapper.findUserSelection(userId, questionId);
    }

    /**
     * 사용자의 옵션 선택을 삭제합니다.
     * @param questionId 질문 ID
     * @param userId 사용자 ID
     * @return 삭제된 행의 수
     */
    @Transactional
    public int deleteUserSelection(int questionId, String userId) {
        return policyQuestionMapper.deleteUserSelection(userId, questionId);
    }

    /**
     * 특정 ID의 옵션을 조회합니다.
     * @param optionId 옵션 ID
     * @return PolicyQuestionOptionsDTO
     */
    public PolicyQuestionOptionsDTO findOptionById(int optionId) {
        return policyQuestionMapper.findOptionById(optionId);
    }

    /**
     * 옵션 목록의 백분율을 계산합니다.
     * @param options 옵션 목록
     */
    private void calculatePercentages(List<PolicyQuestionOptionsDTO> options) {
        // 전체 투표 수 계산
        int totalVotes = options.stream().mapToInt(PolicyQuestionOptionsDTO::getCount).sum();

        // 백분율 계산
        if (totalVotes > 0) {
            for (PolicyQuestionOptionsDTO option : options) {
                double percent = (double) option.getCount() / totalVotes * 100;
                // 소수점 한 자리까지 반올림
                percent = Math.round(percent * 10) / 10.0;
                option.setPercent(percent);
            }
        } else {
            // 투표가 없는 경우 모두 0%로 설정
            options.forEach(option -> option.setPercent(0));
        }
    }
}