package com.example.mockvoting.domain.admin.mapper;

import com.example.mockvoting.domain.admin.dto.AdminDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    List<AdminDTO> findAllUsers();

    // 사용자 활성 상태 업데이트 (회원 탈퇴)
    void updateUserActiveStatus(@Param("userId") String userId, @Param("newStatus") boolean newStatus);

    int updateUserRole(@Param("userId") String userId, @Param("role") String role);
}
