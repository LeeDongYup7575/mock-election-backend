package com.example.mockvoting.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private Long id;
    private String userId;
    private String email;
    private String name;
    private String nickname;
    private String profileImgUrl;
    private LocalDateTime createdAt;
    private boolean active;
    private String role;
    private boolean isElection;
}
