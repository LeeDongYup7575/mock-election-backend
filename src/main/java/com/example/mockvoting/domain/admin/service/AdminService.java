package com.example.mockvoting.domain.admin.service;

import com.example.mockvoting.domain.admin.dto.AdminDTO;
import com.example.mockvoting.domain.admin.dto.PostCountDTO;
import com.example.mockvoting.domain.admin.dto.UserStatsDTO;
import com.example.mockvoting.domain.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

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



    // 사용자 통계 데이터 조회
    public UserStatsDTO getUserStats() {
        try {
            Map<String, List<Integer>> totalUserMap = new HashMap<>();
            Map<String, List<Integer>> newUserMap = new HashMap<>();
            Map<String, List<String>> labelsMap = new HashMap<>();
            Map<String, List<Integer>> totalBoardMap = new HashMap<>(); // 게시물 통계 맵 추가

            // 최근 7일 날짜 라벨 및 인덱스 맵
            List<String> dayLabels = new ArrayList<>();
            Map<String, Integer> dayIndexMap = new HashMap<>();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
                String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd"));
                dayLabels.add(formattedDate);
                dayIndexMap.put(dateStr, 6 - i);
            }

            // 이번 주 요일 라벨 및 인덱스 맵 (주간 데이터를 위한 추가)
            List<String> weekLabels = new ArrayList<>();
            Map<String, Integer> weekIndexMap = new HashMap<>();

            // 이번 주 월요일 구하기
            LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            for (int i = 0; i < 7; i++) {
                LocalDate date = monday.plusDays(i);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String dayOfWeek;

                switch (date.getDayOfWeek()) {
                    case MONDAY: dayOfWeek = "월"; break;
                    case TUESDAY: dayOfWeek = "화"; break;
                    case WEDNESDAY: dayOfWeek = "수"; break;
                    case THURSDAY: dayOfWeek = "목"; break;
                    case FRIDAY: dayOfWeek = "금"; break;
                    case SATURDAY: dayOfWeek = "토"; break;
                    case SUNDAY: dayOfWeek = "일"; break;
                    default: dayOfWeek = "";
                }

                weekLabels.add(dayOfWeek);
                weekIndexMap.put(dateStr, i);
            }

            // 최근 6개월 라벨 및 인덱스 맵
            List<String> monthLabels = new ArrayList<>();
            Map<String, Integer> monthIndexMap = new HashMap<>();

            for (int i = 5; i >= 0; i--) {
                LocalDate date = LocalDate.now().withDayOfMonth(1).minusMonths(i);
                String monthStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM")); // DB 키용
                String monthNum = date.format(DateTimeFormatter.ofPattern("M")); // 1~12 (zero-less)
                String formattedMonth = date.format(DateTimeFormatter.ofPattern("M월"));
                monthLabels.add(formattedMonth);
                int index = 5 - i;
                monthIndexMap.put(monthStr, index);
                monthIndexMap.put(monthNum, index); // 혹시 DB에서 month_num이 올 경우 대비
            }

            // 일간/주간/월간 데이터 초기화
            List<Integer> dayTotalUsers = new ArrayList<>(Collections.nCopies(7, 0));
            List<Integer> dayNewUsers = new ArrayList<>(Collections.nCopies(7, 0));
            List<Integer> weekTotalUsers = new ArrayList<>(Collections.nCopies(7, 0)); // 주간 데이터 초기화
            List<Integer> weekNewUsers = new ArrayList<>(Collections.nCopies(7, 0)); // 주간 데이터 초기화
            List<Integer> monthTotalUsers = new ArrayList<>(Collections.nCopies(6, 0));
            List<Integer> monthNewUsers = new ArrayList<>(Collections.nCopies(6, 0));

            // 일간 총 사용자 수
            List<Map<String, Object>> dailyTotalUsers = adminMapper.getDailyTotalUsers();
            if (dailyTotalUsers != null) {
                for (Map<String, Object> data : dailyTotalUsers) {
                    Object rawDate = data.get("date");
                    Object rawCount = data.get("total_users");
                    if (rawDate != null && rawCount != null) {
                        String date = rawDate.toString();
                        if (date.length() >= 10) date = date.substring(0, 10);

                        // 일간 데이터 처리
                        Integer dayIndex = dayIndexMap.get(date);
                        if (dayIndex != null) {
                            dayTotalUsers.set(dayIndex, ((Number) rawCount).intValue());
                        }

                        // 주간 데이터 처리 (같은 데이터 사용)
                        Integer weekIndex = weekIndexMap.get(date);
                        if (weekIndex != null) {
                            weekTotalUsers.set(weekIndex, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

            // 일간 신규 사용자 수
            List<Map<String, Object>> dailyNewUsers = adminMapper.getDailyNewUsers();
            if (dailyNewUsers != null) {
                for (Map<String, Object> data : dailyNewUsers) {
                    Object rawDate = data.get("date");
                    Object rawCount = data.get("new_users");
                    if (rawDate != null && rawCount != null) {
                        String date = rawDate.toString();
                        if (date.length() >= 10) date = date.substring(0, 10);

                        // 일간 데이터 처리
                        Integer dayIndex = dayIndexMap.get(date);
                        if (dayIndex != null) {
                            dayNewUsers.set(dayIndex, ((Number) rawCount).intValue());
                        }

                        // 주간 데이터 처리 (같은 데이터 사용)
                        Integer weekIndex = weekIndexMap.get(date);
                        if (weekIndex != null) {
                            weekNewUsers.set(weekIndex, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

            // 월간 총 사용자 수
            List<Map<String, Object>> monthlyTotalUsers = adminMapper.getMonthlyTotalUsers();
            if (monthlyTotalUsers != null) {
                for (Map<String, Object> data : monthlyTotalUsers) {
                    Object rawMonth = data.get("month");
                    if (rawMonth != null && data.get("total_users") != null) {
                        String key = rawMonth.toString();
                        Integer index = monthIndexMap.get(key);
                        if (index != null) {
                            monthTotalUsers.set(index, ((Number) data.get("total_users")).intValue());
                        }
                    }
                }
            }

            // 월간 신규 사용자 수
            List<Map<String, Object>> monthlyNewUsers = adminMapper.getMonthlyNewUsers();
            if (monthlyNewUsers != null) {
                for (Map<String, Object> data : monthlyNewUsers) {
                    Object rawMonth = data.get("month");
                    if (rawMonth != null && data.get("new_users") != null) {
                        String key = rawMonth.toString();
                        Integer index = monthIndexMap.get(key);
                        if (index != null) {
                            monthNewUsers.set(index, ((Number) data.get("new_users")).intValue());
                        }
                    }
                }
            }

            // 주간 총 사용자 수
            List<Map<String, Object>> weeklyTotalUsers = adminMapper.getWeeklyTotalUsers();
            if (weeklyTotalUsers != null) {
                for (Map<String, Object> data : weeklyTotalUsers) {
                    Object rawDayOfWeek = data.get("day_of_week");
                    Object rawCount = data.get("total_users");
                    if (rawDayOfWeek != null && rawCount != null) {
                        String dayOfWeek = rawDayOfWeek.toString();
                        // 요일에 맞는 인덱스 찾기
                        int index = weekLabels.indexOf(dayOfWeek);
                        if (index >= 0) {
                            weekTotalUsers.set(index, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

// 주간 신규 사용자 수
            List<Map<String, Object>> weeklyNewUsers = adminMapper.getWeeklyNewUsers();
            if (weeklyNewUsers != null) {
                for (Map<String, Object> data : weeklyNewUsers) {
                    Object rawDayOfWeek = data.get("day_of_week");
                    Object rawCount = data.get("new_users");
                    if (rawDayOfWeek != null && rawCount != null) {
                        String dayOfWeek = rawDayOfWeek.toString();
                        // 요일에 맞는 인덱스 찾기
                        int index = weekLabels.indexOf(dayOfWeek);
                        if (index >= 0) {
                            weekNewUsers.set(index, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

            // 일간/주간/월간 게시물 데이터 초기화
            List<Integer> dayTotalPosts = new ArrayList<>(Collections.nCopies(7, 0));
            List<Integer> weekTotalPosts = new ArrayList<>(Collections.nCopies(7, 0));
            List<Integer> monthTotalPosts = new ArrayList<>(Collections.nCopies(6, 0));

            // 일간 총 게시물 수
            List<Map<String, Object>> dailyTotalPosts = adminMapper.getDailyTotalPosts();
            if (dailyTotalPosts != null) {
                for (Map<String, Object> data : dailyTotalPosts) {
                    Object rawDate = data.get("date");
                    Object rawCount = data.get("total_posts");
                    if (rawDate != null && rawCount != null) {
                        String date = rawDate.toString();
                        if (date.length() >= 10) date = date.substring(0, 10);

                        Integer dayIndex = dayIndexMap.get(date);
                        if (dayIndex != null) {
                            dayTotalPosts.set(dayIndex, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

            // 주간 총 게시물 수
            List<Map<String, Object>> weeklyTotalPosts = adminMapper.getWeeklyTotalPosts();
            if (weeklyTotalPosts != null) {
                for (Map<String, Object> data : weeklyTotalPosts) {
                    Object rawDayOfWeek = data.get("day_of_week");
                    Object rawCount = data.get("total_posts");
                    if (rawDayOfWeek != null && rawCount != null) {
                        String dayOfWeek = rawDayOfWeek.toString();
                        int index = weekLabels.indexOf(dayOfWeek);
                        if (index >= 0) {
                            weekTotalPosts.set(index, ((Number) rawCount).intValue());
                        }
                    }
                }
            }

            // 월간 총 게시물 수
            List<Map<String, Object>> monthlyTotalPosts = adminMapper.getMonthlyTotalPosts();
            if (monthlyTotalPosts != null) {
                for (Map<String, Object> data : monthlyTotalPosts) {
                    Object rawMonth = data.get("month");
                    if (rawMonth != null && data.get("total_posts") != null) {
                        String key = rawMonth.toString();
                        Integer index = monthIndexMap.get(key);
                        if (index != null) {
                            monthTotalPosts.set(index, ((Number) data.get("total_posts")).intValue());
                        }
                    }
                }
            }

            // 결과 구성 (더미 데이터 사용하지 않음)
            totalUserMap.put("day", dayTotalUsers);
            totalUserMap.put("week", weekTotalUsers);
            totalUserMap.put("month", monthTotalUsers);

            newUserMap.put("day", dayNewUsers);
            newUserMap.put("week", weekNewUsers);
            newUserMap.put("month", monthNewUsers);

            labelsMap.put("day", dayLabels);
            labelsMap.put("week", weekLabels);
            labelsMap.put("month", monthLabels);

            totalBoardMap.put("day", dayTotalPosts); // 게시물 통계 추가
            totalBoardMap.put("week", weekTotalPosts); // 게시물 통계 추가
            totalBoardMap.put("month", monthTotalPosts); // 게시물 통계 추가



            return UserStatsDTO.builder()
                    .totalUser(totalUserMap)
                    .newUser(newUserMap)
                    .labels(labelsMap)
                    .totalBoard(totalBoardMap) // 게시물 통계 추가
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}