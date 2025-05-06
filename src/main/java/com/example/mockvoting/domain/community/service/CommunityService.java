package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import com.example.mockvoting.domain.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityMapper communityMapper;

    /**
     *  게시글 카테고리 전체 조회
     */
    public List<CategoryResponseDTO> getAllCategories() {
        return communityMapper.selectAllCategories();
    }

    /**
     *  카테고리별 게시글 조회
     */
    public Page<PostSummaryResponseDTO> getPostsByCategory(String categoryCode, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = (int) pageable.getPageSize();

        List<PostSummaryResponseDTO> posts = communityMapper.selectPostsByCategory(categoryCode, offset, limit);
        int total = communityMapper.selectPostCountByCategory(categoryCode);

        return new PageImpl<>(posts, pageable, total);
    }


}
