package com.example.mockvoting.domain.community.repository;

import com.example.mockvoting.domain.community.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
}
