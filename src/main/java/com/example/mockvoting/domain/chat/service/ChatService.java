package com.example.mockvoting.domain.chat.service;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Collections;

@Service // Service 레이어 - 채팅 메시지 관련 로직 담당
public class ChatService {

    @Autowired // MongoDB 저장소 주입
    private ChatMessageRepository chatMessageRepository;

    // For user info - you'd need to create this repository
//     @Autowired
//     private UserRepository userRepository;

    // 메시지 저장
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        // ID가 없거나 기본값(0)인 경우 null로 설정하여 MongoDB가 자동 생성하도록 함
        if (chatMessage.getId() == null || chatMessage.getId().equals("0")) {
            chatMessage.setId(null);
        }

        return chatMessageRepository.save(chatMessage);
    }

    // 채팅 기록 조회 (최근 50개)
    public List<ChatMessage> getChatHistory(int chatroomId) {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByChatroomIdOrderBySentAtDesc(chatroomId);  // 메서드 호출 시 chatroomId 사용
        Collections.reverse(messages); // 시간순 정렬
        return messages;
    }

    // Perspective API를 이용한 비속어 필터링 (간단한 예시)
    public boolean isProfanity(String content) {
        // 실제로는 Perspective API에 요청하는 코드가 필요
        // 간단한 예시로 몇 가지 비속어 체크
        String[] badWords = {"비속어1", "비속어2", "비속어3"};
        for (String word : badWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // This would fetch user info from your MySQL database
//    public Map<String, Object> getUserInfo(String userId) {
//        Map<String, Object> userInfo = new HashMap<>();
//        // In a real implementation, you'd query your user table
//        // User user = userRepository.findById(userId).orElse(null);
//
//        // For now, return minimal data
//        userInfo.put("userId", userId);
//        userInfo.put("nickname", "User " + userId.substring(0, 5));
//
//        return userInfo;
//    }
}