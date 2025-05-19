package com.example.mockvoting.domain.admin.mapper;

import com.example.mockvoting.domain.admin.dto.AdminDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    List<AdminDTO> findAllUsers();

    // 사용자 활성 상태 업데이트 (회원 탈퇴)
    void updateUserActiveStatus(@Param("userId") String userId, @Param("newStatus") boolean newStatus);

    int updateUserRole(@Param("userId") String userId, @Param("role") String role);


    // 추가: 일별, 월별, 연별 통계 쿼리 메서드
    List<Map<String, Object>> getDailyTotalUsers();
    List<Map<String, Object>> getDailyNewUsers();
    List<Map<String, Object>> getMonthlyTotalUsers();
    List<Map<String, Object>> getMonthlyNewUsers();
    // 주간 총 사용자 및 신규 사용자 수 쿼리
    List<Map<String, Object>> getWeeklyTotalUsers();
    List<Map<String, Object>> getWeeklyNewUsers();

    // 게시물 통계 쿼리 메서드 추가
    List<Map<String, Object>> getDailyTotalPosts();
    List<Map<String, Object>> getWeeklyTotalPosts();
    List<Map<String, Object>> getMonthlyTotalPosts();

}
