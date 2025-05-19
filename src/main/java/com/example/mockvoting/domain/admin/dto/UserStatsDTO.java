package com.example.mockvoting.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Map<String, List<Integer>> totalUser;
    private Map<String, List<Integer>> newUser;
    private Map<String, List<String>> labels;
    private Map<String, List<Integer>> totalBoard; // 게시물 통계 추가
}
