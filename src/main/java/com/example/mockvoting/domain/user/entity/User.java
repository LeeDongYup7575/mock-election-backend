package com.example.mockvoting.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;                // DB 내부 식별자
    private String userId;          // 구글 sub
    private String email;           // 이메일 (구글 이메일)
    private String name;            // 이름 (구글 displayName)
    private String nickname;        // 사용자 닉네임 (추가 입력 받음)
    private String profileImgUrl;   // 프로필 이미지 URL
    private String role;            // 사용자 역할 (USER, ADMIN)
    private LocalDateTime createdAt; // 가입일
    private boolean active;         // 활성 상태 (탈퇴 여부)
    private boolean isElection;     // 선거 참여 여부
    private boolean hasReceivedToken; // 토큰 발급 여부
}