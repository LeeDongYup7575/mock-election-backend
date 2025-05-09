package com.example.mockvoting.domain.user.mapper;

import com.example.mockvoting.domain.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {
    // 사용자 추가
    void insertUser(User user);

    // 사용자ID로 사용자 조회
    Optional<User> findByUserId(String userId);

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 사용자 활성 상태 업데이트 (회원 탈퇴)
    void updateUserActiveStatus(@Param("userId") String userId, @Param("active") boolean active);

    // 회원 완전 삭제
    void deleteUser(@Param("userId") String userId);

    // 사용자 정보 업데이트
    void updateUser(User user);

    // 사용자 투표 상태 업데이트
    void updateUserElectionStatus(@Param("userId") String userId, @Param("isElection") boolean isElection);
}