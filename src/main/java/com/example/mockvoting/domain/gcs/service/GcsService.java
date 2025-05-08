package com.example.mockvoting.domain.gcs.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsService {
    @Value("${gcs.bucket}") private String bucketName;
    @Value("${gcs.path.profiles}") private String profilesPath;
    @Value("${gcs.path.post_images}") private String postImagesPath;
    @Value("${gcs.path.post_attachments}") private String postAttachmentsPath;
    @Value("${gcs.path.feed_images}") private String feedImagesPath;

    private final Storage storage;

    private String resolvePath(String type, String fileName) {
        String prefix;
        switch (type) {
            case "profiles": prefix = profilesPath; break;
            case "post_images": prefix = postImagesPath; break;
            case "post_attachments": prefix = postAttachmentsPath; break;
            case "feed_images": prefix = feedImagesPath; break;
            default:
                log.warn("잘못된 파일 카테고리 요청: {}", type);
                throw new IllegalArgumentException("유효하지 않은 파일 카테고리입니다: " + type);
        }
        return prefix + "/" + fileName;
    }

    public String upload(String type, MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            String extension = originalName.substring(originalName.lastIndexOf('.'));
            String safeFileName = uuid + extension;
            String fullPath = resolvePath(type, safeFileName);

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fullPath)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());
            log.info("파일 업로드 성공: {}", fullPath);
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fullPath);
        } catch (IOException e) {
            log.error("파일 업로드 중 IOException 발생", e);
            throw new RuntimeException("파일 업로드에 실패했습니다.");
        } catch (Exception e) {
            log.error("파일 업로드 중 예외 발생", e);
            throw new RuntimeException("파일 업로드 중 문제가 발생했습니다.");
        }
    }

    public byte[] download(String type, String fileName) {
        try {
            String fullPath = resolvePath(type, fileName);
            BlobId blobId = BlobId.of(bucketName, fullPath);
            Blob target = storage.get(blobId);

            if (target == null || !target.exists()) {
                log.warn("파일이 존재하지 않음: {}", fullPath);
                throw new IllegalArgumentException("요청한 파일이 존재하지 않습니다.");
            }

            log.info("파일 다운로드 성공: {}", fullPath);
            return target.getContent();

        } catch (StorageException e) {
            log.error("GCS 다운로드 오류: {}", e.getMessage());
            throw new RuntimeException("파일 다운로드에 실패했습니다.");
        } catch (Exception e) {
            log.error("파일 다운로드 중 예외 발생", e);
            throw new RuntimeException("파일 다운로드 중 문제가 발생했습니다.");
        }
    }

    public void delete(String type, String fileName) {
        try {
            String fullPath = resolvePath(type, fileName);
            BlobId blobId = BlobId.of(bucketName, fullPath);
            boolean deleted = storage.delete(blobId);

            if (!deleted) {
                log.warn("삭제 실패: 파일이 존재하지 않음 - {}", fullPath);
                throw new IllegalArgumentException("삭제할 파일이 존재하지 않습니다.");
            }

            log.info("파일 삭제 성공: {}", fullPath);
        } catch (StorageException e) {
            log.error("GCS 삭제 오류: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.");
        } catch (Exception e) {
            log.error("파일 삭제 중 예외 발생", e);
            throw new RuntimeException("파일 삭제 중 문제가 발생했습니다.");
        }
    }
}
