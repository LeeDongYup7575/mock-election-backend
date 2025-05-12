package com.example.mockvoting.domain.chat.controller;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.service.ChatService;
import com.example.mockvoting.util.JwtUtil;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private JwtUtil jwtUtil;

//  추후 삭제 : 로깅용 코드
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    // STOMP를 통해 메시지 수신 시 처리
    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage) {

        // 메시지에 현재 시간 설정
        if (chatMessage.getSentAt() == null) {
            chatMessage.setSentAt(new Date());
        }

        // 로그 추가
        System.out.println("메시지 수신: " + chatMessage.getContent() + " 보낸이: " + chatMessage.getSender_nickname());

        // 메시지 저장 및 반환
        return chatService.saveMessage(chatMessage);

//        //메시지 저장
//        ChatMessage saved = chatService.saveMessage(chatMessage);
//
//        //직접 브로드캐스트
//        messagingTemplate.convertAndSend("/topic/public", saved);

    }

    // Optional: endpoint to get user info for the chat
//    @GetMapping("/api/chat/user")
//    @ResponseBody
//    public Map<String, Object> getUserInfo(@RequestParam String userId) {
//        // This would be implemented based on your user service
//        return chatService.getUserInfo(userId);
//    }

    // REST API: 이전 채팅 메시지 조회
    @GetMapping("/api/votings/history/{chatroomId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable int chatroomId) {
        return chatService.getChatHistory(chatroomId);
    }

    // 테스트용 REST 엔드포인트 추가 - WebSocket이 작동하지 않을 때 문제 확인용
    @GetMapping("/api/chat/test")
    @ResponseBody
    public String testChatController() {
        return "Chat Controller is working";
    }
}