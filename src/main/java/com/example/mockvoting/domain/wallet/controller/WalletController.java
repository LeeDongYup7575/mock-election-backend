package com.example.mockvoting.domain.wallet.controller;

import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.wallet.dto.WalletResponseDTO;
import com.example.mockvoting.domain.wallet.entity.Wallet;
import com.example.mockvoting.domain.wallet.mapper.WalletMapper;
import com.example.mockvoting.domain.wallet.service.WalletService;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.exception.CustomException;
import com.example.mockvoting.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;
    private final UserMapper userMapper;

    /**
     * 지갑 연결 API
     */
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> connectWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            log.info("지갑 연결 요청 시작: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            String walletAddress = payload.get("walletAddress");
            log.info("지갑 연결 요청 세부정보: 사용자={}, 지갑주소={}", userId, walletAddress);

            if (walletAddress == null || walletAddress.isEmpty()) {
                log.error("유효하지 않은 지갑 주소: null 또는 빈 문자열");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 지갑 주소가 필요합니다."));
            }

            WalletResponseDTO response = walletService.connectWallet(userId, walletAddress);
            return ResponseEntity.ok(ApiResponse.success("지갑이 성공적으로 연결되었습니다.", response));
        } catch (CustomException e) {
            log.error("지갑 연결 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("지갑 연결 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 새 지갑 생성 API
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> createWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            log.info("새 지갑 생성 요청 시작: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            String walletAddress = payload.get("walletAddress");
            String privateKey = payload.get("privateKey");

            log.info("새 지갑 생성 요청 세부정보: 사용자={}, 지갑주소={}", userId, walletAddress);

            if (walletAddress == null || walletAddress.isEmpty()) {
                log.error("유효하지 않은 지갑 주소: null 또는 빈 문자열");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 지갑 주소가 필요합니다."));
            }

            WalletResponseDTO response = walletService.createNewWallet(userId, walletAddress, privateKey);

            return ResponseEntity.ok(ApiResponse.success("새 지갑이 성공적으로 생성되었습니다.", response));
        } catch (CustomException e) {
            log.error("지갑 생성 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("지갑 생성 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 최초 토큰 발급 API
     */
    @PostMapping("/issue-token")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> issueInitialToken(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            log.info("최초 토큰 발급 요청 시작: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            String walletAddress = payload.get("walletAddress");
            String privateKey = payload.get("privateKey");

            log.info("토큰 발급 요청 세부정보: 사용자={}, 지갑주소={}", userId, walletAddress);

            if (walletAddress == null || walletAddress.isEmpty()) {
                log.error("유효하지 않은 지갑 주소: null 또는 빈 문자열");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 지갑 주소가 필요합니다."));
            }

            WalletResponseDTO response = walletService.issueInitialToken(userId, walletAddress, privateKey);

            return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 발급되었습니다.", response));
        } catch (CustomException e) {
            log.error("토큰 발급 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 발급 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 지갑 상태 조회 API
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getWalletStatus(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("지갑 상태 조회: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            WalletResponseDTO response = walletService.getWalletStatus(userId);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (CustomException e) {
            log.error("지갑 상태 조회 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("지갑 상태 조회 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 토큰 잔액 조회 API
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTokenBalance(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("토큰 잔액 조회: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            int balance = walletService.getTokenBalance(userId);

            return ResponseEntity.ok(ApiResponse.success(Map.of("balance", balance)));
        } catch (CustomException e) {
            log.error("토큰 잔액 조회 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 잔액 조회 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 토큰 차감 API
     */
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> deductToken(
            HttpServletRequest request,
            @RequestBody Map<String, Integer> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            Integer amount = payload.get("amount");

            log.info("토큰 차감 요청: 사용자={}, 금액={}", userId, amount);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            if (amount == null || amount <= 0) {
                log.error("유효하지 않은 차감 금액: {}", amount);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 차감 금액이 필요합니다."));
            }

            WalletResponseDTO response = walletService.deductToken(userId, amount);

            return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 차감되었습니다.", response));
        } catch (CustomException e) {
            log.error("토큰 차감 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 차감 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 지갑 주소 업데이트 API
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> updateWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            String walletAddress = payload.get("walletAddress");

            log.info("지갑 주소 업데이트 요청: 사용자={}, 새 지갑주소={}", userId, walletAddress);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            if (walletAddress == null || walletAddress.isEmpty()) {
                log.error("유효하지 않은 지갑 주소: null 또는 빈 문자열");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 지갑 주소가 필요합니다."));
            }

            // 기존 연결 API 재사용
            WalletResponseDTO response = walletService.connectWallet(userId, walletAddress);

            return ResponseEntity.ok(ApiResponse.success("지갑 주소가 업데이트되었습니다.", response));
        } catch (CustomException e) {
            log.error("지갑 주소 업데이트 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("지갑 주소 업데이트 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 지갑 연결 해제 API
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<ApiResponse<Void>> disconnectWallet(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("지갑 연결 해제 요청: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            walletService.disconnectWallet(userId);

            return ResponseEntity.ok(ApiResponse.success("지갑 연결이 해제되었습니다.", null));
        } catch (CustomException e) {
            log.error("지갑 연결 해제 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("지갑 연결 해제 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 메타마스크 지갑 연결 API
     */
    @PostMapping("/connect/metamask")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> connectMetaMaskWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        try {
            String userId = (String) request.getAttribute("userId");
            log.info("메타마스크 지갑 연결 요청 시작: 사용자={}", userId);

            if (userId == null || userId.isEmpty()) {
                log.error("인증되지 않은 요청 - userId가 없음");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("인증이 필요합니다. 다시 로그인해주세요."));
            }

            String walletAddress = payload.get("walletAddress");
            log.info("메타마스크 지갑 연결 요청 세부정보: 사용자={}, 지갑주소={}", userId, walletAddress);

            if (walletAddress == null || walletAddress.isEmpty()) {
                log.error("유효하지 않은 지갑 주소: null 또는 빈 문자열");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효한 지갑 주소가 필요합니다."));
            }

            // 메타마스크 지갑 연결 서비스 호출
            WalletResponseDTO response = walletService.connectMetaMaskWallet(userId, walletAddress);
            return ResponseEntity.ok(ApiResponse.success("메타마스크 지갑이 성공적으로 연결되었습니다.", response));
        } catch (CustomException e) {
            log.error("메타마스크 지갑 연결 중 사용자 정의 예외 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("메타마스크 지갑 연결 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서버 오류: " + e.getMessage()));
        }
    }

    /**
     * 수동 토큰 발급 API (추가)
     */
    @PostMapping("/issue-token-manual")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> issueTokenManually(
            HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            log.info("수동 토큰 발급 요청: userId={}", userId);

            // 사용자 확인
            User user = userMapper.findByUserId(userId)
                    .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

            // 이미 토큰을 받은 경우 발급하지 않음
            if (user.isHasReceivedToken()) {
                Wallet wallet = walletMapper.findByUserId(userId)
                        .orElseThrow(() -> new CustomException("지갑이 연결되어 있지 않습니다."));

                return ResponseEntity.ok(ApiResponse.success("이미 토큰을 보유하고 있습니다.",
                        WalletResponseDTO.builder()
                                .walletAddress(wallet.getWalletAddress())
                                .tokenBalance(wallet.getTokenBalance())
                                .walletType(wallet.getWalletType())
                                .connected(true)
                                .build()));
            }

            // 지갑 확인
            Wallet wallet = walletMapper.findByUserId(userId)
                    .orElseThrow(() -> new CustomException("지갑이 연결되어 있지 않습니다."));

            // 토큰 발급
            wallet.setTokenBalance(1);
            walletMapper.updateTokenBalance(userId, 1);
            userMapper.updateUserTokenStatus(userId, true);

            return ResponseEntity.ok(ApiResponse.success("토큰이 발급되었습니다.",
                    WalletResponseDTO.builder()
                            .walletAddress(wallet.getWalletAddress())
                            .tokenBalance(1)
                            .walletType(wallet.getWalletType())
                            .connected(true)
                            .build()));

        } catch (Exception e) {
            log.error("수동 토큰 발급 오류: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("토큰 발급 중 오류가 발생했습니다."));
        }
    }
}