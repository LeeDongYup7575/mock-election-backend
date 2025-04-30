package com.example.mockvoting.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String userId;
    private String email;
    private String name;
    private String nickname;
    private String profileImgUrl;
    private String role;
    private LocalDateTime createdAt;
    private boolean isElection;
}