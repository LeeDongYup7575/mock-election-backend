package com.example.mockvoting.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "unread_count")
public class UnreadCountDTO {
    private int id;
    private int messageId;
    private String userId;
    private int chatroomId;
}
