package com.example.mockvoting.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
    private String token;
    private String userId;
    private String role;

    // 추가된 사용자 프로필 정보
    private String email;
    private String name;
    private String nickname;
    private String profileImgUrl;
}