package com.example.mockvoting.domain.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voter_id", "target_type", "target_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voter_id", nullable = false, length = 50)
    private String voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "vote", nullable = false)
    private byte vote;  // 1 for upvote, -1 for downvote

    public enum TargetType {
        POST,
        POST_COMMENT,
        FEED,
        FEED_COMMENT
    }
}
