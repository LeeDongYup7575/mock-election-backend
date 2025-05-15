package com.example.mockvoting.domain.chat.service;

import com.example.mockvoting.domain.chat.entity.Membership;
import com.example.mockvoting.domain.chat.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;

    // 채팅방 ID로 멤버십 정보 조회 메서드 추가
    public List<Membership> findByChatroomId(int chatroomId) {
        return membershipRepository.findByChatroomId(chatroomId);
    }
}
