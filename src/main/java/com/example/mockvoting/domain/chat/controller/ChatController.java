package com.example.mockvoting.domain.chat.controller;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.service.ChatService;
import com.example.mockvoting.util.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    // STOMP를 통해 메시지 수신 시 처리
    @MessageMapping("/chat/{chatroomId}")
    @SendTo("/topic/{chatroomId}")
    public ChatMessage sendMessage(@DestinationVariable int chatroomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {

        // 현재 메시지에 설정된 userId 저장
        String requestUserId = chatMessage.getUserId();

        // HTTP 요청과 연결된 헤더에서 JWT 토큰 찾기
        String jwt = null;
        if (headerAccessor.getSessionAttributes() != null &&
                headerAccessor.getSessionAttributes().containsKey("jwt")) {
            jwt = (String) headerAccessor.getSessionAttributes().get("jwt");
        }

        // JWT 토큰이 있으면 검증
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            String authenticatedUserId = jwtUtil.getUserIdFromToken(jwt);
            // 요청한 userId와 토큰의 userId가 같은지 확인
            if (!authenticatedUserId.equals(requestUserId)) {
                // 허용되지 않은 사용자 ID 시 수정
                chatMessage.setUserId(authenticatedUserId);
            }
        }

        // 비속어 검사
        if(chatService.isProfanity(chatMessage.getContent())) {
            // 비속어 감지 시 처리
            chatMessage.setContent("비속어가 감지되어 차단되었습니다.");
        }

        // 메시지 저장 및 반환
        return chatService.saveMessage(chatMessage);
    }

    // REST API: 이전 채팅 메시지 조회
    @GetMapping("/api/chat/{chatroomId}/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable int chatroomId) {
        return chatService.getChatHistory(chatroomId);
    }
}