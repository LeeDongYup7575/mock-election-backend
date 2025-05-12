package com.example.mockvoting.domain.community.service;

import com.example.mockvoting.domain.community.dto.*;
import com.example.mockvoting.domain.community.entity.Post;
import com.example.mockvoting.domain.community.entity.PostAttachment;
import com.example.mockvoting.domain.community.mapper.CategoryMapper;
import com.example.mockvoting.domain.community.mapper.PostAttachmentMapper;
import com.example.mockvoting.domain.community.mapper.PostMapper;
import com.example.mockvoting.domain.community.mapper.converter.PostDtoMapper;
import com.example.mockvoting.domain.community.repository.PostAttachmentRepository;
import com.example.mockvoting.domain.community.repository.PostRepository;
import com.example.mockvoting.domain.gcs.service.GcsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final PostDtoMapper postDtoMapper;
    private final GcsService gcsService;

    /**
     *  게시글 상세 조회
     */
    @Transactional
    public PostDetailViewDTO getPostDetail(Integer id, String viewedPostIds) {
        // 1) 조회 여부 판단 - 조회하는 게시글이 이전에 사용자가 조회한 게시글인가?
        List<String> viewedList = viewedPostIds.isEmpty()
                ? Collections.emptyList()
                : Arrays.asList(viewedPostIds.split("-"));
        boolean alreadyViewed = viewedList.contains(id.toString());

        String updatedViewedPostIds = null;
        if (!alreadyViewed) {
            // 2) 조회수 증가
            postMapper.updateViewCountById(id);
            // 3) 쿠키에 담을 새 값 계산
            updatedViewedPostIds = viewedPostIds.isEmpty()
                    ? id.toString()
                    : viewedPostIds + "-" + id;
        }

        // 게시글 조회
        PostDetailResponseDTO detail = postMapper.selectPostDetailById(id);
        // 첨부파일 조회
        List<PostAttachmentResponseDTO> attachments = postAttachmentMapper.selectAttachmentsByPostId(id);
        detail.setAttachments(attachments);

        return PostDetailViewDTO.builder()
                .post(detail)
                .newViewedPostIds(updatedViewedPostIds)
                .build();
    }

    /**
     *  카테고리별 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDTO> getPostsByCategory(String categoryCode, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = (int) pageable.getPageSize();

        List<PostSummaryResponseDTO> posts = postMapper.selectPostsByCategory(categoryCode, offset, limit);
        int total = categoryMapper.selectPostCountByCategory(categoryCode);

        return new PageImpl<>(posts, pageable, total);
    }

    /**
     *  게시글 등록
     */
//    @Transactional
//    public Long save(PostCreateRequestDTO dto) {
//        Post post = postDtoMapper.toEntity(dto);
//
//        // 썸네일이 없으면 기본 이미지 경로로 설정
//        if (post.getThumbnailUrl() == null || post.getThumbnailUrl().isBlank()) {
//            post.setThumbnailUrl("https://storage.googleapis.com/visionvote_uploads/post/images/default_thumnail.jpg");
//        }
//
//        return postRepository.save(post).getId();
//    }

    /**
     *  게시글 등록 (첨부파일 포함)
     */
    @Transactional
    public Long save(PostCreateRequestDTO dto, List<MultipartFile> files) {
        Post post = postDtoMapper.toEntity(dto);

        // 썸네일이 없으면 기본 이미지 경로로 설정
        if (post.getThumbnailUrl() == null || post.getThumbnailUrl().isBlank()) {
            post.setThumbnailUrl("https://storage.googleapis.com/visionvote_uploads/post/images/default_thumnail.jpg");
        }

        Long id = postRepository.save(post).getId();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String url = gcsService.upload("post_attachments", file);

                PostAttachment attachment = PostAttachment.builder()
                        .postId(id)
                        .name(file.getOriginalFilename())
                        .url(url)
                        .size(file.getSize())
                        .deleted(false)
                        .build();

                postAttachmentRepository.save(attachment);
            }
        }

        return id;
    }

    /**
     *  게시글 삭제
     */
    @Transactional
    public void delete(Long postId, String requesterId) {
        // 게시글 존재 확인 및 소유자 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다"));

        if (!post.getAuthorId().equals(requesterId)) {
            throw new SecurityException("게시글 삭제 권한이 없습니다");
        }

        // 게시글 soft delete
        post.setDeleted(true);

        // 첨부파일 soft delete
        List<PostAttachment> attachments = postAttachmentRepository.findByPostId(postId);
        for (PostAttachment attachment : attachments) {
            attachment.setDeleted(true);
        }
    }
}
