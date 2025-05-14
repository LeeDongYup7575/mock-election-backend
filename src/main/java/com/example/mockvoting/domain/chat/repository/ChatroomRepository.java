package com.example.mockvoting.domain.chat.repository;

import com.example.mockvoting.domain.chat.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Integer> {
    // 기본 메서드로 충분: findAll(), findById(), save(), deleteById() 등 자동 제공
}
