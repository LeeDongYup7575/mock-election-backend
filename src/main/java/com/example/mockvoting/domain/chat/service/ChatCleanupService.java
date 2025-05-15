package com.example.mockvoting.domain.chat.service;

import com.example.mockvoting.domain.chat.entity.ChatMessage;
import com.example.mockvoting.domain.chat.repository.ChatroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatCleanupService {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MongoTemplate mongoTemplate; // MongoTemplate 추가

    // 채팅방별 최대 메시지 수
    private static final int MAX_MESSAGES_PER_ROOM = 50;

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupMessages() {
        try {
            // 모든 채팅방 ID 조회
            List<Integer> chatroomIds = chatroomRepository.findAll().stream()
                    .map(room -> room.getId())
                    .toList();

            // 각 채팅방별로 정리
            for (Integer chatroomId : chatroomIds) {
                try {
                    // MongoDB에서 직접 카운트 쿼리 실행
                    Query countQuery = new Query(Criteria.where("chatroomId").is(chatroomId));
                    long count = mongoTemplate.count(countQuery, ChatMessage.class, "chat_message");

                    // 최대 개수를 초과하면 오래된 메시지 삭제
                    if (count > MAX_MESSAGES_PER_ROOM) {
                        // 삭제할 개수 계산
                        long deleteCount = count - MAX_MESSAGES_PER_ROOM;

                        // 오래된 메시지부터 조회
                        Query query = new Query(Criteria.where("chatroomId").is(chatroomId))
                                .with(Sort.by(Sort.Direction.ASC, "sentAt"))
                                .limit((int) deleteCount);

                        List<ChatMessage> oldestMessages = mongoTemplate.find(query, ChatMessage.class, "chat_message");

                        if (!oldestMessages.isEmpty()) {
                            // 개별 삭제 (일괄 삭제가 작동하지 않을 경우)
                            for (ChatMessage msg : oldestMessages) {
                                try {
                                    mongoTemplate.remove(msg, "chat_message");
                                } catch (Exception e) {
                                    System.err.println("메시지 삭제 실패: " + msg.getId() + " - " + e.getMessage());
                                }
                            }
                            System.out.println("채팅방 " + chatroomId + "에서 " + oldestMessages.size() + "개의 오래된 메시지 삭제 시도 완료");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("메시지 정리 작업 전체 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
