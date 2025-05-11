package com.example.mockvoting.domain.chat.dto;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
public class ChatMessageDTO {

    @Id
    private String id; // String 타입으로 변경하여 MongoDB가 자동 생성하는 ObjectId 사용

    private String type;
    private String content;
    private Date sentAt;
    private int chatroomId;
    private String userId;
    private String sender_nickname;

}
