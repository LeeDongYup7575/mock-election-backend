package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CommunityMainResponseDTO;
import com.example.mockvoting.domain.community.dto.CommunityStatsDTO;
import com.example.mockvoting.domain.community.dto.NoticeSummaryDTO;
import com.example.mockvoting.domain.community.dto.PopularPostResponseDTO;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import com.example.mockvoting.domain.community.mapper.CommunityMapper;
import com.example.mockvoting.domain.community.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityMapper communityMapper;
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;

    public CommunityMainResponseDTO getMainInfo() {
        // 1) 회원 수, 게시글 수, 댓글 수 조회 for HeroSection
        CommunityStatsDTO communityStats = communityMapper.selectCommunityStats();

        // 2) 인기 게시글 조회 for PopularPostsSection
        List<PopularPostResponseDTO> popularPosts = postMapper.selectPopularPosts();
        for (PopularPostResponseDTO post : popularPosts) {
            // 2-1) summaryContent 가공
            String text = Jsoup.parse(post.getSummaryContent()).text();
            String summary = text.length() <= 100 ? text : text.substring(0, 100) + "...";
            post.setSummaryContent(summary);

            // 2-2) 익명 여부 판단
            boolean isAnonymous = categoryMapper.selectIsAnonymousById(post.getCategoryId());
            if (isAnonymous) {
                post.setAuthorNickname("익명");
            }
        }

        // 3) 최근 공지사항 조회
        List<NoticeSummaryDTO> recentNotices = postMapper.selectRecentNotices();

        // 3-1) summaryContent 가공
        for (int i = 0; i < recentNotices.size(); i++) {
            String summary = Jsoup.parse(recentNotices.get(i).getSummaryContent()).text();
            if (i == 0) {
                summary = summary.length() <= 200 ? summary : summary.substring(0, 200) + "...";
            } else {
                summary = "";
            }
            recentNotices.get(i).setSummaryContent(summary);
        }

        return CommunityMainResponseDTO.builder()
                .communityStats(communityStats)
                .popularPosts(popularPosts)
                .recentNotices(recentNotices)
                .build();
    }
}
