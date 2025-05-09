package com.example.mockvoting.domain.wallet.controller;

import com.example.mockvoting.domain.wallet.dto.WalletResponseDTO;
import com.example.mockvoting.domain.wallet.service.WalletService;
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

    /**
     * 지갑 연결 API
     */
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> connectWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        String userId = (String) request.getAttribute("userId");
        String walletAddress = payload.get("walletAddress");

        log.info("지갑 연결 요청: 사용자={}, 지갑주소={}", userId, walletAddress);

        WalletResponseDTO response = walletService.connectWallet(userId, walletAddress);

        return ResponseEntity.ok(ApiResponse.success("지갑이 성공적으로 연결되었습니다.", response));
    }

    /**
     * 새 지갑 생성 API
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> createWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        String userId = (String) request.getAttribute("userId");
        String walletAddress = payload.get("walletAddress");
        String privateKey = payload.get("privateKey");

        log.info("새 지갑 생성 요청: 사용자={}, 지갑주소={}", userId, walletAddress);

        WalletResponseDTO response = walletService.createNewWallet(userId, walletAddress, privateKey);

        return ResponseEntity.ok(ApiResponse.success("새 지갑이 성공적으로 생성되었습니다.", response));
    }

    /**
     * 지갑 상태 조회 API
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getWalletStatus(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("지갑 상태 조회: 사용자={}", userId);

        WalletResponseDTO response = walletService.getWalletStatus(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 토큰 잔액 조회 API
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTokenBalance(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        int balance = walletService.getTokenBalance(userId);

        return ResponseEntity.ok(ApiResponse.success(Map.of("balance", balance)));
    }

    /**
     * 지갑 주소 업데이트 API
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> updateWallet(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        String userId = (String) request.getAttribute("userId");
        String walletAddress = payload.get("walletAddress");

        log.info("지갑 주소 업데이트 요청: 사용자={}, 새 지갑주소={}", userId, walletAddress);

        WalletResponseDTO response = walletService.updateWalletAddress(userId, walletAddress);

        return ResponseEntity.ok(ApiResponse.success("지갑 주소가 업데이트되었습니다.", response));
    }

    /**
     * 지갑 연결 해제 API
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<ApiResponse<Void>> disconnectWallet(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        log.info("지갑 연결 해제 요청: 사용자={}", userId);

        walletService.disconnectWallet(userId);

        return ResponseEntity.ok(ApiResponse.success("지갑 연결이 해제되었습니다.", null));
    }
}