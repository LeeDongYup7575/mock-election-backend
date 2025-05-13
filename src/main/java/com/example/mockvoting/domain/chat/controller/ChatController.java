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
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable int roomId, ChatMessage chatMessage) {

        // 로그 추가
        System.out.println("채팅방 ID: " + roomId + ", 메시지 수신: " + chatMessage.getContent() + ", 보낸이: " + chatMessage.getSender_nickname());

        // 채팅방 ID 설정
        chatMessage.setChatroomId(roomId);

        // 메시지에 현재 시간 설정
        if (chatMessage.getSentAt() == null) {
            chatMessage.setSentAt(new Date());
        }

        // 메시지 저장
        ChatMessage savedMessage = chatService.saveMessage(chatMessage);

        // 지정된 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);
    }


    // 테스트용 REST 엔드포인트 추가 - WebSocket이 작동하지 않을 때 문제 확인용
    @GetMapping("/api/chat/test")
    @ResponseBody
    public String testChatController() {
        return "Chat Controller is working";
    }
}