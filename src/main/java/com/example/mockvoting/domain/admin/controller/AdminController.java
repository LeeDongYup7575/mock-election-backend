package com.example.mockvoting.domain.admin.controller;

import com.example.mockvoting.domain.admin.dto.AdminDTO;
import com.example.mockvoting.domain.admin.dto.PostCountDTO;
import com.example.mockvoting.domain.admin.dto.UserStatsDTO;
import com.example.mockvoting.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 전체 사용자 조회
    @GetMapping("/users")
    public ResponseEntity<List<AdminDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/{userId}/toggle-active")
    public ResponseEntity<Void> toggleUserActiveStatus(
            @PathVariable String userId,
            @RequestParam boolean targetStatus) {

        adminService.toggleUserActiveStatus(userId, targetStatus);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable String userId,
            @RequestParam String targetRole) {
        adminService.updateUserRoleStatus(userId, targetRole);
        return ResponseEntity.ok().build();
    }



    // 추가: 사용자 통계 데이터 조회
    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        return ResponseEntity.ok(adminService.getUserStats());
    }



}
