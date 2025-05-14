package com.example.mockvoting.domain.admin.service;

import com.example.mockvoting.domain.admin.dto.AdminDTO;
import com.example.mockvoting.domain.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;

    public List<AdminDTO> getAllUsers() {
        return adminMapper.findAllUsers();
    }

    @Transactional
    public void toggleUserActiveStatus(String userId, boolean targetStatus) {
        boolean newStatus = !targetStatus;
        adminMapper.updateUserActiveStatus(userId, newStatus);
    }

    @Transactional
    public void updateUserRoleStatus(String userId, String targetRole) {
        // 현재 역할에 따라 반대 역할로 설정
        String newRole = "USER".equalsIgnoreCase(targetRole) ? "ADMIN" : "USER";
        adminMapper.updateUserRole(userId, newRole);
    }
}
