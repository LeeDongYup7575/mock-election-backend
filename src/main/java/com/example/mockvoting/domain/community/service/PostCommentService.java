package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.CommunityVoteResultDTO;
import com.example.mockvoting.domain.community.dto.PostCommentCreateRequestDTO;
import com.example.mockvoting.domain.community.dto.PostCommentResponseDTO;
import com.example.mockvoting.domain.community.dto.PostCommentUpdateRequestDTO;
import com.example.mockvoting.domain.community.entity.CommunityVote;
import com.example.mockvoting.domain.community.entity.PostComment;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import com.example.mockvoting.domain.community.mapper.CommunityVoteMapper;
import com.example.mockvoting.domain.community.mapper.PostCommentMapper;
import com.example.mockvoting.domain.community.mapper.PostMapper;
import com.example.mockvoting.domain.community.mapper.converter.PostCommentDtoMapper;
import com.example.mockvoting.domain.community.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final CommunityVoteMapper communityVoteMapper;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentDtoMapper postCommentDtoMapper;


    /**
     *  최상위 댓글 조회
     */
//    @Transactional
//    public List<PostCommentResponseDTO> getTopLevelCommentsByPostId(Long postId, int offset, int limit) {
//        return postCommentMapper.selectTopLevelCommentsByPostId(postId, offset, limit);
//    }

    /**
     *  댓글 조회
     */
    @Transactional(readOnly = true)
    public List<PostCommentResponseDTO> getCommentsWithReplies(Long postId, int offset, int limit, String userId) {
        // 1) 최상위 댓글
        Map<String,Object> params = Map.of("postId", postId, "offset", offset, "limit", limit);
        List<PostCommentResponseDTO> top = postCommentMapper.selectTopLevelComments(params);
        if (top.isEmpty()) return Collections.emptyList();

        // 2) depth 1~4 자식 댓글 반복 조회
        List<PostCommentResponseDTO> all = new ArrayList<>(top);
        List<Long> parentIds = top.stream()
                .map(PostCommentResponseDTO::getId)
                .collect(Collectors.toList());
        for (int depth = 1; depth <= 4 && !parentIds.isEmpty(); depth++) {
            List<PostCommentResponseDTO> children = postCommentMapper.selectCommentsByParentIds(parentIds);
            if (children.isEmpty()) break;
            all.addAll(children);
            parentIds = children.stream()
                    .map(PostCommentResponseDTO::getId)
                    .collect(Collectors.toList());
        }

        // 2-1) 익명 게시판 여부 확인 및 시간 순 익명 매핑 (삭제 필터링 이전)
        Long categoryId     = postMapper.selectCategoryIdById(postId);
        boolean isAnonymous = categoryMapper.selectIsAnonymousById(categoryId);
        String postAuthorId = postMapper.selectAuthorIdById(postId);
        if (isAnonymous) {
            List<PostCommentResponseDTO> byTime = new ArrayList<>(all);
            byTime.sort(Comparator.comparing(PostCommentResponseDTO::getCreatedAt));

            Map<String, String> anonymousMap = new LinkedHashMap<>();
            int counter = 1;
            for (PostCommentResponseDTO comment : byTime) {
                String authorId = comment.getAuthorId();
                if (authorId.equals(postAuthorId)) {
                    comment.setAnonymousNickname("익명(글쓴이)");
                } else {
                    anonymousMap.putIfAbsent(authorId, "익명 " + counter++);
                    comment.setAnonymousNickname(anonymousMap.get(authorId));
                }
                comment.setAuthorNickname(null);
            }
        }

        // 3) parentId → List<child> 매핑
        Map<Long, List<PostCommentResponseDTO>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(PostCommentResponseDTO::getParentId));

        // 4) “삭제된 댓글 + 살아있는 후손 없음” 필터링
        Set<Long> toRemove = new HashSet<>();
        for (PostCommentResponseDTO c : all) {
            if (c.getIsDeleted() && !hasLiveDescendant(c.getId(), childrenMap)) {
                toRemove.add(c.getId());
            }
        }

        // 5) 사용자 투표 정보 매핑 (userVote)
        if (userId != null) {
            List<Long> ids = all.stream().map(PostCommentResponseDTO::getId).toList();
            if (!ids.isEmpty()) {
                List<CommunityVoteResultDTO> voteList = communityVoteMapper.selectVotesByVoterAndTargetIds(
                        userId, CommunityVote.TargetType.POST_COMMENT, ids
                );

                Map<Long, Byte> voteMap = voteList.stream()
                        .collect(Collectors.toMap(CommunityVoteResultDTO::getTargetId, CommunityVoteResultDTO::getVote));

                for (PostCommentResponseDTO comment : all) {
                    comment.setUserVote(voteMap.get(comment.getId())); // null-safe
                }
            }
        }

        // 6) 최종 트리 재귀 순회로 결과 리스트 생성
        List<PostCommentResponseDTO> result = new ArrayList<>();
        for (PostCommentResponseDTO root : top) {
            if (!toRemove.contains(root.getId())) {
                traverse(root, childrenMap, toRemove, result);
            }
        }
        return result;
    }

    // 살아있는 후손이 하나라도 있으면 true
    private boolean hasLiveDescendant(Long id, Map<Long, List<PostCommentResponseDTO>> childrenMap) {
        for (PostCommentResponseDTO child : childrenMap.getOrDefault(id, List.of())) {
            if (!child.getIsDeleted() || hasLiveDescendant(child.getId(), childrenMap)) {
                return true;
            }
        }
        return false;
    }

    // DFS로 트리 순회하며 toRemove 검사 후 결과에 추가
    private void traverse(PostCommentResponseDTO node,
                          Map<Long, List<PostCommentResponseDTO>> childrenMap,
                          Set<Long> toRemove,
                          List<PostCommentResponseDTO> out) {
        out.add(node);
        for (PostCommentResponseDTO child : childrenMap.getOrDefault(node.getId(), List.of())) {
            if (!toRemove.contains(child.getId())) {
                traverse(child, childrenMap, toRemove, out);
            }
        }
    }

    /**
     *  댓글 등록
     */
    @Transactional
    public Long save(Long PostId, PostCommentCreateRequestDTO dto, String requesterId) {
        int depth = 0;
        if(dto.getParentId() != null) {
            PostComment parentComment = postCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글이 존재하지 않습니다."));
            depth = parentComment.getDepth() + 1;
        }
        if (depth > 4) {
            throw new IllegalArgumentException("답글은 최대 depth 4까지만 작성할 수 있습니다.");
        }
        dto.setAuthorId(requesterId);
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

    /**
     *  댓글 수정
     */
    @Transactional
    public void update(Long commentId, String requesterId, PostCommentUpdateRequestDTO dto) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getAuthorId().equals(requesterId)) {
            throw new SecurityException("댓글 수정 권한이 없습니다.");
        }

        comment.update(dto.getContent());
    }

}
