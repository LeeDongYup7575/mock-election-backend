package com.example.mockvoting.domain.user.dto;

import lombok.Data;

@Data
public class OAuth2RequestDTO {
    private String token;      // 구글에서 받은 ID 토큰
    private String provider;   // 제공자 (google)
}
