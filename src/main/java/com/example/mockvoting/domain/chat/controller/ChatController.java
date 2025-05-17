package com.example.mockvoting.domain.chat.controller;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.entity.Membership;
import com.example.mockvoting.domain.chat.repository.MembershipRepository;
import com.example.mockvoting.domain.chat.service.ChatService;
import com.example.mockvoting.domain.chat.service.MembershipService;
import com.example.mockvoting.domain.user.dto.UserResponseDTO;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.service.UserService;
import com.example.mockvoting.util.JwtUtil;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private UserService userService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MembershipRepository membershipRepository;

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

        // 비속어 감지 및 필터링
        System.out.println("비속어 감지 메서드 호출 직전...");
        //테스트용
        boolean containsProfanity = false;
        try {
            containsProfanity = chatService.checkAndFilterProfanity(chatMessage);
            System.out.println("비속어 감지 결과: " + containsProfanity);
        } catch (Exception e) {
            System.err.println("비속어 감지 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }

        // 필요한 경우 비속어 감지 결과에 따라 다른 처리 가능
        if (containsProfanity) {
            // 예: 사용자에게 경고 메시지 전송
            ChatMessage warningMessage = new ChatMessage();
            warningMessage.setSender_nickname("System");
            warningMessage.setContent("비속어 사용은 채팅 규정에 위반됩니다. 반복 시 제재를 받을 수 있습니다.");
            warningMessage.setSentAt(new Date());
            warningMessage.setChatroomId(roomId);
            warningMessage.setId(null);

            // 경고 메시지 저장 및 전송
            ChatMessage savedWarning = chatService.saveMessage(warningMessage);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedWarning);

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

    // 사용자 채팅방 참여 처리
    @MessageMapping("/chat.join/{roomId}")
    public void handleJoin(@DestinationVariable int roomId, @Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String nickname = payload.get("nickname");

        System.out.println("사용자 채팅방 참여: userId = " + userId + ", nickname = " + nickname + ", roomId = " + roomId);

        // 멤버십 생성 또는 업데이트
        createOrUpdateMembership(userId,roomId);

        // 참여 메시지 생성
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender_nickname", "System");
        chatMessage.put("content", nickname + " 님이 참여했습니다.");
        chatMessage.put("sentAt", new Date());
        chatMessage.put("userId", null);  // 시스템 메시지 표시
        chatMessage.put("chatroomId", roomId); // 채팅방 ID 추가

        // 여기서 시스템 메시지를 DB에 저장
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setSender_nickname("System");
        systemMessage.setContent(nickname + " 님이 참여했습니다.");
        systemMessage.setSentAt(new Date());
        systemMessage.setUserId(null);
        systemMessage.setChatroomId(roomId);

        // 저장
        chatService.saveMessage(systemMessage);

        // 모든 참여자에게 참여 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);

        // 최신 참여자 목록 전송
        sendParticipantsList(roomId);
    }

    // 사용자 채팅방 퇴장 처리
    @MessageMapping("/chat.leave/{roomId}")
    public void handleLeave(@DestinationVariable int roomId, @Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String nickname = payload.get("nickname");

        System.out.println("사용자 채팅방 퇴장: userId = " + userId + ", roomId = " + roomId);

        // 멤버십 삭제
        removeMembership(userId, roomId);

        // 퇴장 메시지 생성
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender_nickname", "System");
        chatMessage.put("content", nickname + " 님이 퇴장했습니다.");
        chatMessage.put("sentAt", new Date());
        chatMessage.put("userId", null);  // 시스템 메시지 표시

        // 시스템 메시지를 DB에 저장
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setSender_nickname("System");
        systemMessage.setContent(nickname + " 님이 퇴장했습니다.");
        systemMessage.setSentAt(new Date());
        systemMessage.setUserId(null);
        systemMessage.setChatroomId(roomId);

        // 저장
        chatService.saveMessage(systemMessage);

        // 모든 참여자에게 퇴장 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);

        // 최신 참여자 목록 전송
        sendParticipantsList(roomId);
    }

    // 전체 참여자 목록 전송
    private void sendParticipantsList(int roomId) {
        List<Map<String, Object>> participants = getParticipantsList(roomId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "participants_list");
        message.put("participants", participants);
        message.put("timestamp", new Date());

        messagingTemplate.convertAndSend("/topic/participants/" + roomId, message);
    }

    // 채팅방 참여자 목록 조회 API
    @GetMapping("/api/chat/participants/{roomId}")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(@PathVariable int roomId){
        List<Map<String, Object>> participants = getParticipantsList(roomId);
        return ResponseEntity.ok(participants);
    }

    // 참여자 목록 조회 공통 메서드
    public List<Map<String, Object>> getParticipantsList(int roomId) {

        // 채팅방 멤버십 정보 조회
        List<Membership> memberships = membershipService.findByChatroomId(roomId);

        // 채팅방 멤버십 정보에서 사용자 목록 조회
        return memberships.stream()
                .map(membership -> {
                    // 사용자 정보 조회
                    Optional<UserResponseDTO> userResponseDTO = userService.getUserInfo(membership.getUserId());

                    if(userResponseDTO.isPresent()) {
                        UserResponseDTO userDto = userResponseDTO.get();

                        // Optional에서 값을 추출할 때, 값이 없으면 처리할 방법을 정의
                        Map<String, Object> participant = new HashMap<>();
                        participant.put("id", userDto.getUserId());
                        participant.put("nickname", userDto.getNickname());
                        participant.put("role", membership.isRole() ? "admin" : "user");

                        return participant;
                    }else {
                        // 사용자 정보가 없을 경우 예외 처리하거나 기본값 설정
                        return null;
                    }
                })
                .filter(Objects::nonNull) // null 값 제외
                .collect(Collectors.toList());
    }

    // 멤버십 생성 또는 업데이트
    private void createOrUpdateMembership(String userId, int roomId) {
        // 먼저 기존 멤버십 확인
        Optional<Membership> existingMembership = membershipRepository.findByUserIdAndChatroomId(userId, roomId);

        if(existingMembership.isPresent()) {
            System.out.println("기존 멤버십 존재: userId = " + userId + ", chatroomId = " + roomId);
        } else {
            // 새 멤버십 생성
            Membership membership = Membership.builder()
                    .userId(userId)
                    .chatroomId(roomId)
                    .role(false) //일반 사용자
                    .joinedAt(new java.sql.Date(System.currentTimeMillis()))
                    .build();

            membershipRepository.save(membership);
            System.out.println("새 멤버십 생성됨: userId = " + userId + ", chatroomId = " + roomId);
        }
    }

    // 멤버십 삭제
    private void removeMembership(String userId, int roomId) {
        membershipRepository.deleteByUserIdAndChatroomId(userId,roomId);
    }
}