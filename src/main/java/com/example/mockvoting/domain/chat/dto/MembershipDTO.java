package com.example.mockvoting.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDTO {
    private int id;
    private String userId;
    private int chatroomId;
    private boolean role;
    private Date joinedAt;
}
