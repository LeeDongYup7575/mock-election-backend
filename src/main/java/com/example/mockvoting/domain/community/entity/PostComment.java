package com.example.mockvoting.domain.community.entity;

import com.example.mockvoting.domain.common.Votable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_comment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment implements Votable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "author_id", nullable = false, length = 50)
    private String authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private int upvotes = 0;

    @Column(nullable = false)
    private int downvotes = 0;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public int getUpvotes() { return this.upvotes; }

    @Override
    public int getDownvotes() { return this.downvotes; }

    @Override
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    @Override
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
}
