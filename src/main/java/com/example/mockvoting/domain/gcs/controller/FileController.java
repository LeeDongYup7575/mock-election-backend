package com.example.mockvoting.domain.gcs.controller;

import com.example.mockvoting.domain.gcs.service.GcsService;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final GcsService gcsService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(@RequestParam String type,
                                                        @RequestParam MultipartFile file) {
        log.info("파일 업로드 요청: type={}, filename={}", type, file.getOriginalFilename());

        try {
            String url = gcsService.upload(type, file);
            log.info("파일 업로드 요청 처리 성공: type={}, url={}", type, url);
            return ResponseEntity.ok(ApiResponse.success("파일 업로드 성공", url));
        } catch (Exception e) {
            log.error("파일 업로드 요청 처리 실패: type={}, filename={}", type, file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("파일 업로드 실패"));
        }
    }
}
