package com.example.mockvoting.domain.chat.repository;

import com.example.mockvoting.domain.chat.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    // 채팅방 ID로 모든 멤버십 조회
    List<Membership> findByChatroomId(int chatroomId);

    // 사용자ID와 채팅방ID로 멤버십 조회
    Optional<Membership> findByUserIdAndChatroomId(String userId, Integer chatroomId);

    // 사용자ID와 채팅방ID로 멤버십 삭제
    @Transactional
    void deleteByUserIdAndChatroomId(String userId, int chatroomId);
}
