package com.example.mockvoting.domain.chat.service;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;

@Service // Service 레이어 - 채팅 메시지 관련 로직 담당
public class ChatService {

    @Autowired // MongoDB 저장소 주입
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PerspectiveApiService perspectiveApiService;

    // 메시지 저장
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        // ID가 없거나 기본값(0)인 경우 null로 설정하여 MongoDB가 자동 생성하도록 함
        if (chatMessage.getId() == null || chatMessage.getId().equals("0")) {
            chatMessage.setId(null);
        }

        // 필터링 먼저 수행 (동기적으로)
        boolean isToxic = checkAndFilterProfanity(chatMessage);

        if (isToxic) {
            System.out.println("비속어 감지됨 (저장 전 필터링 완료): " + chatMessage.getContent());
        }

        // 필터링된 메시지 저장
        return chatMessageRepository.save(chatMessage);
    }

    // 비속어 감지와 필터링을 위한 동기 메소드 (메시지 전송 전 체크)
    public boolean checkAndFilterProfanity(ChatMessage chatMessage) {

        try{
            // 비동기 호출을 동기 호출로 변환하여 결과를 기다립니다
            boolean isToxic = perspectiveApiService.containsToxicContent(chatMessage.getContent()).block();

            if (isToxic) {
                // 비속어가 감지되면 메시지 내용을 수정하거나 경고 추가
                chatMessage.setContent("[비속어가 감지되어 메시지가 필터링되었습니다]");
                // 또는 관리자에게 알림 등 추가 조치
            }
            return isToxic;
        }catch (Exception e){
            return false; // 오류 발생 시 비속어가 없다고 간주
        }

    }

    // 채팅 기록 조회 (최근 50개)
    public List<ChatMessage> getChatHistory(int chatroomId) {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByChatroomIdOrderBySentAtDesc(chatroomId);  // 메서드 호출 시 chatroomId 사용
        Collections.reverse(messages); // 시간순 정렬
        return messages;
    }


}