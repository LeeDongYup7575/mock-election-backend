package com.example.mockvoting.domain.spamcheck.service;

import com.example.mockvoting.domain.spamcheck.dto.PostContentCheckDTO;
import com.example.mockvoting.domain.spamcheck.mapper.SpamCheckMapper;
import com.example.mockvoting.domain.spamcheck.model.SpamCheckType;
import com.example.mockvoting.util.SpamSimilarityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpamCheckService {
    private final SpamCheckMapper spamCheckMapper;
    private final SpamSimilarityUtil similarityUtil;

    /**
     * 도배 의심 여부 판단 (게시글/댓글 공용)
     */
    public boolean isSuspicious(String userId, SpamCheckType type, String title, String content) {
        LocalDateTime now = LocalDateTime.now();

        // 조건 1: 1분 내 작성 기록
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);
        int recentCount = switch (type) {
            case POST -> spamCheckMapper.selectRecentPosts(userId, oneMinuteAgo).size();
            case POST_COMMENT -> spamCheckMapper.selectRecentComments(userId, oneMinuteAgo).size();
            default -> 0;
        };
        if (recentCount >= 2) return true;

        // 조건 2: 10분 내 유사 내용 여부
        LocalDateTime tenMinutesAgo = now.minusMinutes(10);
        String current = (title != null ? title : "") + content;

        switch (type) {
            case POST -> {
                List<PostContentCheckDTO> recentPosts = spamCheckMapper.selectRecentPosts(userId, tenMinutesAgo);
                for (PostContentCheckDTO post : recentPosts) {
                    String combined = post.getTitle() + post.getContent();
                    if (similarityUtil.calculateSimilarity(current, combined) >= 0.8) return true;
                }
            }
            case POST_COMMENT -> {
                List<String> recentComments = spamCheckMapper.selectRecentComments(userId, tenMinutesAgo);
                for (String prev : recentComments) {
                    if (similarityUtil.calculateSimilarity(current, prev) >= 0.8) return true;
                }
            }
            default -> {}
        }

        return false;
    }
}
