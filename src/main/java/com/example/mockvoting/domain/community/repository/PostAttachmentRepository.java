package com.example.mockvoting.domain.community.repository;

import com.example.mockvoting.domain.community.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
}
