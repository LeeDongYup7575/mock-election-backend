package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.PostCreateRequestDTO;
import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.dto.PostSummaryResponseDTO;
import com.example.mockvoting.domain.community.entity.Post;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import com.example.mockvoting.domain.community.mapper.PostMapper;
import com.example.mockvoting.domain.community.mapper.converter.PostDtoMapper;
import com.example.mockvoting.domain.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final PostRepository postRepository;
    private final PostDtoMapper postDtoMapper;

    /**
     *  게시글 상세 조회
     */
    public PostDetailResponseDTO getPostDetail(Integer id) {
        return postMapper.selectPostDetailById(id);
    }

    /**
     *  카테고리별 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDTO> getPostsByCategory(String categoryCode, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = (int) pageable.getPageSize();

        List<PostSummaryResponseDTO> posts = postMapper.selectPostsByCategory(categoryCode, offset, limit);
        int total = categoryMapper.selectPostCountByCategory(categoryCode);

        return new PageImpl<>(posts, pageable, total);
    }

    /**
     *  게시글 등록
     */
    @Transactional
    public Long save(PostCreateRequestDTO dto) {
        Post post = postDtoMapper.toEntity(dto);
        return postRepository.save(post).getId();
    }
}
