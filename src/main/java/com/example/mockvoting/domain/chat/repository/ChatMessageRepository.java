package com.example.mockvoting.domain.chat.repository;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatroomIdOrderBySentAtAsc(int chatroomId);
    List<ChatMessage> findTop50ByChatroomIdOrderBySentAtDesc(int chatroomId);
}
