package com.example.mockvoting.domain.community.dto;

import com.example.mockvoting.domain.community.entity.CommunityVote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityVoteRequestDTO {
    private CommunityVote.TargetType targetType;
    private Long targetId;
    private byte vote;  // 1: upvote, -1: downvote, 0: 취소
}
