package com.example.mockvoting.domain.user.service;

import com.example.mockvoting.domain.gcs.service.GcsService;
import com.example.mockvoting.domain.user.dto.OAuth2RequestDTO;
import com.example.mockvoting.domain.user.dto.TokenResponseDTO;
import com.example.mockvoting.domain.user.dto.UserResponseDTO;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.exception.CustomException;
import com.example.mockvoting.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final GoogleService googleService;
    private final GcsService gcsService;

    /**
     * 구글 로그인/회원가입 처리
     */
    @Transactional
    public TokenResponseDTO googleLogin(OAuth2RequestDTO request) {
        // 구글 토큰 검증
        Map<String, Object> googleUserInfo = googleService.verifyGoogleToken(request.getToken());

        String googleId = (String) googleUserInfo.get("sub");
        String email = (String) googleUserInfo.get("email");
        String name = (String) googleUserInfo.get("name");
        String pictureUrl = (String) googleUserInfo.get("pictureUrl");

        // 이메일로 기존 사용자 찾기
        Optional<User> existingUser = userMapper.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            user = existingUser.get();

            // 구글 ID가 없는 경우 (이메일로만 가입된 경우) 연결
            if (user.getUserId() == null || !user.getUserId().equals(googleId)) {
                user.setUserId(googleId);
            }

            // 프로필 이미지 업데이트 (구글 이미지가 있는 경우)
            if (pictureUrl != null && !pictureUrl.isEmpty()) {
                user.setProfileImgUrl(pictureUrl);
            }

            // 이름 업데이트
            if (name != null && !name.isEmpty()) {
                user.setName(name);
            }

            // 정보 업데이트
            userMapper.updateUser(user);
            log.info("기존 사용자 구글 로그인: {}", email);
        } else {
            // 신규 사용자 생성
            user = User.builder()
                    .userId(googleId)
                    .email(email)
                    .name(name)
                    .nickname(name)
                    .profileImgUrl(pictureUrl != null ? pictureUrl : "/images/profiles/default-profile.png")
                    .role("USER")
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .isElection(false)
                    .build();

            userMapper.insertUser(user);
            log.info("신규 구글 사용자 가입: {}", email);
        }

        // 회원 탈퇴한 사용자 체크
        if (!user.isActive()) {
            throw new CustomException("탈퇴한 사용자입니다.");
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());

        return TokenResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .role(user.getRole())
                .build();
    }

    /**
     * 회원 탈퇴 처리
     */
    @Transactional
    public void deleteUser(String userId) {
        // 사용자 존재 여부 확인
        Optional<User> userOpt = userMapper.findByUserId(userId);

        if (userOpt.isEmpty()) {
            throw new CustomException("존재하지 않는 사용자입니다.");
        }

        userMapper.deleteUser(userId);

        // 사용자 비활성화 (논리적 삭제)
        // userMapper.updateUserActiveStatus(userId, false);
    }

    /**
     * 사용자 활성화 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isActiveUser(String userId) {
        Optional<User> userOpt = userMapper.findByUserId(userId);
        return userOpt.map(User::isActive).orElse(false);
    }

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserInfo(String userId) {
        return userMapper.findByUserId(userId)
                .map(user -> UserResponseDTO.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .nickname(user.getNickname())
                        .profileImgUrl(user.getProfileImgUrl())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .isElection(user.isElection())
                        .build());
    }

    @Transactional
    public UserResponseDTO updateNicknameAndProfile(
            String userId,
            String nickname,
            MultipartFile profileImage
    ) {
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다."));

        user.setNickname(nickname);

        // 프로필 이미지가 넘어오면 GCS에 업로드하고 URL 저장
        if (profileImage != null && !profileImage.isEmpty()) {
            // "profiles" 는 application.yml이나 @Value("${gcs.path.profiles}") 에 설정된 프로필 폴더 키
            String imageUrl = gcsService.upload("profiles", profileImage);
            user.setProfileImgUrl(imageUrl);
        }

        userMapper.updateUser(user);  // 이 메서드는 nickname, profile_img_url만 바꿔도 OK

        return getUserInfo(userId)
                .orElseThrow(() -> new CustomException("수정된 사용자 정보를 찾을 수 없습니다."));
    }


    /**
     * 사용자 투표 상태 업데이트
     */
    @Transactional
    public UserResponseDTO updateElectionStatus(String userId, boolean status) {
        // 사용자 존재 여부 확인
        Optional<User> userOpt = userMapper.findByUserId(userId);

        if (userOpt.isEmpty()) {
            throw new CustomException("존재하지 않는 사용자입니다.");
        }

        // 투표 상태 업데이트
        userMapper.updateUserElectionStatus(userId, status);

        // 업데이트된 사용자 정보 반환
        return getUserInfo(userId)
                .orElseThrow(() -> new CustomException("사용자 정보를 찾을 수 없습니다."));
    }
}