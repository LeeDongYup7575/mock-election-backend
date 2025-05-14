package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.PostCommentCreateRequestDTO;
import com.example.mockvoting.domain.community.dto.PostCommentResponseDTO;
import com.example.mockvoting.domain.community.entity.PostComment;
import com.example.mockvoting.domain.community.mapper.PostCommentMapper;
import com.example.mockvoting.domain.community.mapper.converter.PostCommentDtoMapper;
import com.example.mockvoting.domain.community.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentMapper postCommentMapper;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentDtoMapper postCommentDtoMapper;


    /**
     *  최상위 댓글 조회
     */
    @Transactional
    public List<PostCommentResponseDTO> getTopLevelCommentsByPostId(Long postId, int offset, int limit) {
        return postCommentMapper.selectTopLevelCommentsByPostId(postId, offset, limit);
    }

    /**
     *  댓글 등록
     */
    @Transactional
    public Long save(Long PostId, PostCommentCreateRequestDTO dto) {
        int depth = 0;
        if(dto.getParentId() != null) {
            PostComment parentComment = postCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글이 존재하지 않습니다."));
            depth = parentComment.getDepth() + 1;
        }
        dto.setPostId(PostId);
        dto.setDepth(depth);

        PostComment postComment = postCommentDtoMapper.toEntity(dto);
        Long id = postCommentRepository.save(postComment).getId();

        return id;
    }

    /**
     *  댓글 삭제
     */
    @Transactional
    public void delete(Long commentId, String requesterId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다"));

        if (!comment.getAuthorId().equals(requesterId)) {
            throw new SecurityException("댓글 삭제 권한이 없습니다");
        }

        comment.setDeleted(true);
    }
}
