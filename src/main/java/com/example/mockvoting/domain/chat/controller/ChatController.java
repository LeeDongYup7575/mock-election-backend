package com.example.mockvoting.domain.chat.controller;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // STOMP를 통해 메시지 수신 시 처리
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable int chatroomId, @Payload ChatMessage chatMessage) {
        // 비속어 검사
        if(chatService.isProfanity(chatMessage.getContent())) {
            // 비속어 감지 시 처리
            chatMessage.setContent("비속어가 감지되어 차단되었습니다.");
        }

        // 메시지 저장 및 반환
        return chatService.saveMessage(chatMessage);
    }

    // REST API: 이전 채팅 메시지 조회
    @GetMapping("/api/chat/{roomId}/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable int chatroomId) {
        return chatService.getChatHistory(chatroomId);
    }
}