package com.example.mockvoting.domain.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

    private int id;
    private String userId;
    private int chatroomId;
    private boolean role;
    private Date joinedAt;

}
