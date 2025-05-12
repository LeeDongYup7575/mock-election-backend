package com.example.mockvoting.domain.community.repository;

import com.example.mockvoting.domain.community.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    List<PostAttachment> findByPostId(Long postId);
}
