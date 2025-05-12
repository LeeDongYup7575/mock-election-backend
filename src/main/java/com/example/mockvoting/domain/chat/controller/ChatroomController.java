package com.example.mockvoting.domain.chat.controller;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.entity.Chatroom;
import com.example.mockvoting.domain.chat.repository.ChatroomRepository;
import com.example.mockvoting.domain.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatroomController {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private ChatService chatService;

    // 모든 채팅방 조회
    @GetMapping("/rooms")
    public List<Chatroom> getAllChatrooms() {
        return chatroomRepository.findAll();
    }

    // 특정 채팅방 기록 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessage>> getChatroomMessages(@PathVariable int roomId) {
        // ChatService에 이미 구현된 메서드 활용
        List<ChatMessage> messages = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(messages);
    }

}
