package com.example.mockvoting.domain.wallet.service;

import com.example.mockvoting.domain.wallet.dto.WalletResponseDTO;
import com.example.mockvoting.domain.wallet.entity.Wallet;
import com.example.mockvoting.domain.wallet.mapper.WalletMapper;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;
    private final UserMapper userMapper;

    /**
     * 사용자 지갑 연결
     * - 새 지갑을 연결하거나 기존 지갑 정보를 업데이트
     * - 사용자당 하나의 지갑만 가질 수 있음
     */
    @Transactional
    public WalletResponseDTO connectWallet(String userId, String walletAddress) {
        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            log.error("유효하지 않은 지갑 주소: {}", walletAddress);
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 사용자 확인
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자 ID로 사용자를 찾을 수 없음: {}", userId);
                    return new CustomException("사용자를 찾을 수 없습니다.");
                });

        // 이미 존재하는 지갑인지 확인
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);

        // 지갑 연결 또는 업데이트 처리
        Wallet wallet;
        boolean isNewWallet = false;

        if (existingWallet.isPresent()) {
            // 기존 지갑 정보 업데이트
            wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);
            log.info("기존 지갑 정보 업데이트: userId={}, walletAddress={}", userId, walletAddress);
        } else {
            // 새 지갑 정보 저장
            isNewWallet = true;
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .tokenBalance(0) // 초기 토큰은 0으로 설정 (토큰 발급은 별도 프로세스)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);
            log.info("새 지갑 연결 완료: userId={}, walletAddress={}", userId, walletAddress);
        }

        // 사용자가 아직 토큰을 받지 않았고, 최초 지갑 연결인 경우 토큰 발급
        if (isNewWallet && !user.isHasReceivedToken()) {
            // 초기 토큰 10개 지급
            wallet.setTokenBalance(10);
            walletMapper.updateTokenBalance(userId, 10);

            // 토큰 발급 여부 업데이트
            userMapper.updateUserTokenStatus(userId, true);
            log.info("초기 토큰 발급 완료: userId={}, tokenBalance=10", userId);
        }

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .connected(true)
                .build();
    }

    /**
     * 새 지갑 생성
     * - 사용자에게 새 지갑을 생성하고 등록
     * - 최초 생성 시에만 토큰 발급
     */
    @Transactional
    public WalletResponseDTO createNewWallet(String userId, String walletAddress, String privateKey) {
        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            log.error("유효하지 않은 지갑 주소: {}", walletAddress);
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 사용자 확인
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자 ID로 사용자를 찾을 수 없음: {}", userId);
                    return new CustomException("사용자를 찾을 수 없습니다.");
                });

        // 이미 존재하는 지갑인지 확인
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);

        Wallet wallet;
        boolean isNewWallet = false;

        if (existingWallet.isPresent()) {
            // 기존 지갑 정보 업데이트
            wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);
            log.info("기존 지갑 정보 업데이트(새 지갑): userId={}, walletAddress={}", userId, walletAddress);
        } else {
            // 새 지갑 정보 저장
            isNewWallet = true;
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey(privateKey)
                    .tokenBalance(0) // 초기 토큰은 0으로 설정 (토큰 발급은 별도 처리)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);
            log.info("새 지갑 생성 완료: userId={}, walletAddress={}", userId, walletAddress);
        }

        // 사용자가 아직 토큰을 받지 않았고, 최초 지갑 생성인 경우 토큰 발급
        if (!user.isHasReceivedToken()) {
            // 초기 토큰 10개 지급
            wallet.setTokenBalance(10);
            walletMapper.updateTokenBalance(userId, 10);

            // 토큰 발급 여부 업데이트
            userMapper.updateUserTokenStatus(userId, true);
            log.info("초기 토큰 발급 완료: userId={}, tokenBalance=10", userId);
        }

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .connected(true)
                .build();
    }

    /**
     * 사용자 지갑 상태 확인
     */
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletStatus(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        return walletOpt.map(wallet -> WalletResponseDTO.builder()
                        .walletAddress(wallet.getWalletAddress())
                        .tokenBalance(wallet.getTokenBalance())
                        .connected(true)
                        .build())
                .orElse(WalletResponseDTO.builder()
                        .connected(false)
                        .build());
    }

    /**
     * 토큰 잔액 조회
     */
    @Transactional(readOnly = true)
    public int getTokenBalance(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        return walletOpt.map(Wallet::getTokenBalance).orElse(0);
    }

    /**
     * 토큰 차감 (투표 참여 시)
     */
    @Transactional
    public WalletResponseDTO deductToken(String userId, int amount) {
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        if (amount <= 0) {
            log.error("유효하지 않은 차감 금액: {}", amount);
            throw new CustomException("유효하지 않은 차감 금액입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isEmpty()) {
            log.error("연결된 지갑이 없음: {}", userId);
            throw new CustomException("연결된 지갑이 없습니다.");
        }

        Wallet wallet = walletOpt.get();

        // 토큰 잔액 확인
        if (wallet.getTokenBalance() < amount) {
            log.error("토큰 잔액 부족: 현재={}, 필요={}", wallet.getTokenBalance(), amount);
            throw new CustomException("토큰 잔액이 부족합니다.");
        }

        // 토큰 차감
        int newBalance = wallet.getTokenBalance() - amount;
        wallet.setTokenBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletMapper.updateTokenBalance(userId, newBalance);

        log.info("토큰 차감 완료: userId={}, amount={}, 남은 토큰={}", userId, amount, newBalance);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(newBalance)
                .connected(true)
                .build();
    }

    /**
     * 최초 토큰 발급
     * - 사용자당 최초 1회만 발급
     */
    @Transactional
    public WalletResponseDTO issueInitialToken(String userId, String walletAddress, String privateKey) {
        // 사용자 확인
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자 ID로 사용자를 찾을 수 없음: {}", userId);
                    return new CustomException("사용자를 찾을 수 없습니다.");
                });

        // 토큰 발급 여부 확인
        if (user.isHasReceivedToken()) {
            log.warn("이미 토큰을 발급받은 사용자: {}", userId);
            throw new CustomException("이미 토큰을 발급받았습니다.");
        }

        // 새 지갑 생성 또는 기존 지갑 연결
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);

        Wallet wallet;
        if (existingWallet.isPresent()) {
            wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setTokenBalance(10); // 초기 토큰 10개 지급
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);
        } else {
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey(privateKey)
                    .tokenBalance(10) // 초기 토큰 10개 지급
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            walletMapper.insertWallet(wallet);
        }

        // 사용자 토큰 발급 상태 업데이트
        userMapper.updateUserTokenStatus(userId, true);

        log.info("최초 토큰 발급 완료: userId={}, tokenBalance=10", userId);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .connected(true)
                .build();
    }


    /**
     * 지갑 연결 해제
     * - 지갑 정보 완전히 삭제
     */
    @Transactional
    public void disconnectWallet(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        // 사용자의 지갑 정보가 있는지 확인
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isPresent()) {
            // 지갑 정보 완전히 삭제 (저장하지 않고 삭제)
            walletMapper.deleteWallet(userId);
            log.info("지갑 연결 해제 완료: userId={}", userId);
        } else {
            log.info("지갑 연결 해제 요청 - 연결된 지갑 없음: userId={}", userId);
        }
    }
}