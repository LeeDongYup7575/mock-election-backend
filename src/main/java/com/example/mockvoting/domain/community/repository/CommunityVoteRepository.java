package com.example.mockvoting.domain.community.repository;

import com.example.mockvoting.domain.community.entity.CommunityVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityVoteRepository extends JpaRepository<CommunityVote, Long> {
    Optional<CommunityVote> findByVoterIdAndTargetTypeAndTargetId(String voterId, CommunityVote.TargetType targetType, Long targetId);
}
