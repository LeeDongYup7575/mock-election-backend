package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.common.Votable;
import com.example.mockvoting.domain.community.dto.CommunityVoteRequestDTO;
import com.example.mockvoting.domain.community.entity.CommunityVote;
import com.example.mockvoting.domain.community.entity.Post;
import com.example.mockvoting.domain.community.entity.PostComment;
import com.example.mockvoting.domain.community.mapper.converter.CommunityVoteDtoMapper;
import com.example.mockvoting.domain.community.repository.CommunityVoteRepository;
import com.example.mockvoting.domain.community.repository.PostCommentRepository;
import com.example.mockvoting.domain.community.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityVoteService {
    private final CommunityVoteRepository communityVoteRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final CommunityVoteDtoMapper communityVoteDtoMapper;


    /**
     *  커뮤니티 투표 요청 처리
     */
    @Transactional
    public void processVote(CommunityVoteRequestDTO dto, String voterId) {

        switch (dto.getTargetType()) {
            case POST -> processPostVote(dto, voterId);
            case POST_COMMENT -> processPostCommentVote(dto, voterId);
            case FEED, FEED_COMMENT -> {
                // TODO: 추후 구현 예정
            }
            default -> throw new IllegalArgumentException("유효하지 않은 targetType입니다: " + dto.getTargetType());
        }
    }

    // 게시글 투표 처리
    private void processPostVote(CommunityVoteRequestDTO dto, String voterId) {
        Long targetId = dto.getTargetId();
        byte newVote = dto.getVote();

        Post post = postRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 사용자의 기존 투표 여부 확인
        Optional<CommunityVote> existingOpt = communityVoteRepository.findByVoterIdAndTargetTypeAndTargetId(
                voterId, CommunityVote.TargetType.POST, targetId
        );

        if (existingOpt.isEmpty()) {
            // 해당 타겟에 대한 첫 투표
            CommunityVote newEntity = communityVoteDtoMapper.toEntiy(dto);
            newEntity.setVoterId(voterId);
            communityVoteRepository.save(newEntity);
            applyVote(post, newVote);
        } else {
            // 이전에 해당 타겟에 투표 이력 존재
            CommunityVote existing = existingOpt.get();
            byte oldVote = existing.getVote();

            if (newVote == oldVote) {   // 이미 투표한 버튼 재클릭 시 취소
                communityVoteRepository.delete(existing);
                applyVote(post, (byte) -oldVote);
            } else {    // 다른 투표(upvote <-> downvote)로 변경 시 처리
                existing.setVote(newVote);
                applyVote(post, (byte) (newVote - oldVote));
            }
        }

        postRepository.save(post);
    }

    // 게시글 댓글 투표 처리
    private void processPostCommentVote(CommunityVoteRequestDTO dto, String voterId) {
        Long targetId = dto.getTargetId();
        byte newVote = dto.getVote();

        // 1. 댓글 조회
        PostComment comment = postCommentRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 2. 기존 투표 여부 확인
        Optional<CommunityVote> existingOpt = communityVoteRepository.findByVoterIdAndTargetTypeAndTargetId(
                voterId, CommunityVote.TargetType.POST_COMMENT, targetId
        );

        if (existingOpt.isEmpty()) {
            // 첫 투표
            CommunityVote newEntity = communityVoteDtoMapper.toEntiy(dto);
            newEntity.setVoterId(voterId);
            communityVoteRepository.save(newEntity);
            applyVote(comment, newVote);
        } else {
            CommunityVote existing = existingOpt.get();
            byte oldVote = existing.getVote();

            if (newVote == oldVote) {
                // 동일한 투표 재클릭 → 취소
                communityVoteRepository.delete(existing);
                applyVote(comment, (byte) -oldVote);
            } else {
                // 투표 변경
                existing.setVote(newVote);
                applyVote(comment, (byte) (newVote - oldVote));
            }
        }

        postCommentRepository.save(comment);
    }


    // 투표 적용
    private void applyVote(Votable target, byte delta) {
        switch (delta) {
            case 1 -> target.setUpvotes(target.getUpvotes() + 1);        // upvotes +1
            case -1 -> target.setDownvotes(target.getDownvotes() + 1);   // downvotes +1
            case 2 -> {  // downvote → upvote
                target.setUpvotes(target.getUpvotes() + 1);
                target.setDownvotes(target.getDownvotes() - 1);
            }
            case -2 -> { // upvote → downvote
                target.setUpvotes(target.getUpvotes() - 1);
                target.setDownvotes(target.getDownvotes() + 1);
            }
            default -> {
                // 아무 것도 하지 않음 (예외 처리 필요 없다면 비워둬도 됨)
            }
        }
    }

}
