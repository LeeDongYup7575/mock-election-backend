package com.example.mockvoting.domain.user.controller;
import com.example.mockvoting.domain.user.dto.OAuth2RequestDTO;
import com.example.mockvoting.response.ApiResponse;
import com.example.mockvoting.domain.user.dto.TokenResponseDTO;
import com.example.mockvoting.domain.user.dto.UserResponseDTO;
import com.example.mockvoting.domain.user.service.UserService;
import com.example.mockvoting.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 구글 로그인 API (회원가입 포함)
     */
    @PostMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> googleLogin(@RequestBody OAuth2RequestDTO request) {
        log.info("구글 로그인 요청");
        TokenResponseDTO tokenResponse = userService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("구글 로그인이 완료되었습니다.", tokenResponse));
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    /**
     * 사용자 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMyInfo(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("사용자 정보 조회: {}", userId);

        return userService.getUserInfo(userId)
                .map(userResponse -> ResponseEntity.ok(ApiResponse.success(userResponse)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다.")));
    }

    /**
     * 사용자 정보 수정 API
     * multipart/form-data 로 이메일, 이름, 닉네임, 프로필이미지 수신
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMyInfo(
            HttpServletRequest request,
            @RequestPart("nickname") String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        String userId = (String) request.getAttribute("userId");
        log.info("사용자 닉네임/프로필 수정 요청: {}, nickname={}", userId, nickname);

        UserResponseDTO updatedUser =
                userService.updateNicknameAndProfile(userId, nickname, profileImage);

        return ResponseEntity.ok(ApiResponse.success("닉네임 및 프로필이 수정되었습니다.", updatedUser));
    }


    /**
     * 회원 탈퇴 API
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("회원 탈퇴 요청: {}", userId);

        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
    }

    /**
     * 사용자 투표 상태 업데이트 API
     */
    @PatchMapping("/me/election-status")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateElectionStatus(
            HttpServletRequest request,
            @RequestParam(value = "status", defaultValue = "true") boolean status) {

        String userId = (String) request.getAttribute("userId");
        log.info("사용자 투표 상태 업데이트: {}, 상태: {}", userId, status);

        UserResponseDTO updatedUser = userService.updateElectionStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("투표 상태가 업데이트되었습니다.", updatedUser));
    }
}