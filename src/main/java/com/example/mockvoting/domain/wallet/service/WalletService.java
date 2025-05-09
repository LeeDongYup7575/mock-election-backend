package com.example.mockvoting.domain.wallet.service;

import com.example.mockvoting.domain.wallet.dto.WalletResponseDTO;
import com.example.mockvoting.domain.wallet.entity.Wallet;
import com.example.mockvoting.domain.wallet.mapper.WalletMapper;
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

    /**
     * 사용자 지갑 연결
     */
    @Transactional
    public WalletResponseDTO connectWallet(String userId, String walletAddress) {
        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 이미 존재하는 지갑인지 확인
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);

        if (existingWallet.isPresent()) {
            // 이미 연결된 지갑이 있으면 업데이트
            Wallet wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);

            log.info("기존 지갑 정보 업데이트: userId={}, walletAddress={}", userId, walletAddress);

            return WalletResponseDTO.builder()
                    .walletAddress(wallet.getWalletAddress())
                    .tokenBalance(wallet.getTokenBalance())
                    .connected(true)
                    .build();
        } else {
            // 새 지갑 정보 저장
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .tokenBalance(10) // 초기 토큰 10개 지급
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);

            log.info("새 지갑 연결 완료: userId={}, walletAddress={}", userId, walletAddress);

            return WalletResponseDTO.builder()
                    .walletAddress(wallet.getWalletAddress())
                    .tokenBalance(wallet.getTokenBalance())
                    .connected(true)
                    .build();
        }
    }

    /**
     * 새 지갑 생성
     */
    @Transactional
    public WalletResponseDTO createNewWallet(String userId, String walletAddress, String privateKey) {
        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 이미 존재하는 지갑인지 확인
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);

        if (existingWallet.isPresent()) {
            // 이미 연결된 지갑이 있으면 업데이트
            Wallet wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);

            log.info("기존 지갑 정보 업데이트(새 지갑): userId={}, walletAddress={}", userId, walletAddress);

            return WalletResponseDTO.builder()
                    .walletAddress(wallet.getWalletAddress())
                    .tokenBalance(wallet.getTokenBalance())
                    .connected(true)
                    .build();
        } else {
            // 새 지갑 정보 저장
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey(privateKey)
                    .tokenBalance(10) // 초기 토큰 10개 지급
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);

            log.info("새 지갑 생성 완료: userId={}, walletAddress={}", userId, walletAddress);

            return WalletResponseDTO.builder()
                    .walletAddress(wallet.getWalletAddress())
                    .tokenBalance(wallet.getTokenBalance())
                    .connected(true)
                    .build();
        }
    }

    /**
     * 사용자 지갑 상태 확인
     */
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletStatus(String userId) {
        if (userId == null || userId.isEmpty()) {
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
     * 지갑 주소 업데이트
     */
    @Transactional
    public WalletResponseDTO updateWalletAddress(String userId, String newWalletAddress) {
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (newWalletAddress == null || newWalletAddress.isEmpty()) {
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isEmpty()) {
            throw new CustomException("연결된 지갑이 없습니다.");
        }

        Wallet wallet = walletOpt.get();
        wallet.setWalletAddress(newWalletAddress);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletMapper.updateWallet(wallet);

        log.info("지갑 주소 업데이트 완료: userId={}, newWalletAddress={}", userId, newWalletAddress);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .connected(true)
                .build();
    }

    /**
     * 토큰 잔액 조회
     */
    @Transactional(readOnly = true)
    public int getTokenBalance(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        return walletOpt.map(Wallet::getTokenBalance).orElse(0);
    }

    /**
     * 토큰 잔액 업데이트
     */
    @Transactional
    public WalletResponseDTO updateTokenBalance(String userId, int tokenBalance) {
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        if (tokenBalance < 0) {
            throw new CustomException("토큰 잔액은 0 이상이어야 합니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isEmpty()) {
            throw new CustomException("연결된 지갑이 없습니다.");
        }

        Wallet wallet = walletOpt.get();
        wallet.setTokenBalance(tokenBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletMapper.updateWallet(wallet);

        log.info("토큰 잔액 업데이트 완료: userId={}, tokenBalance={}", userId, tokenBalance);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .connected(true)
                .build();
    }

    /**
     * 지갑 연결 해제
     */
    @Transactional
    public void disconnectWallet(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        // 사용자의 지갑 정보가 있는지 확인
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isPresent()) {
            walletMapper.deleteWallet(userId);
            log.info("지갑 연결 해제 완료: userId={}", userId);
        } else {
            log.info("지갑 연결 해제 요청 - 연결된 지갑 없음: userId={}", userId);
        }
    }
}