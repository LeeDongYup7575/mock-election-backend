package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.PostDetailResponseDTO;
import com.example.mockvoting.domain.community.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;

    /**
     *  게시글 상세 조회
     */
    public PostDetailResponseDTO getPostDetail(Integer id) {
        return postMapper.selectPostDetailById(id);
    }
}
