// WalletMapper.java 업데이트
package com.example.mockvoting.domain.wallet.mapper;

import com.example.mockvoting.domain.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface WalletMapper {
    // 지갑 정보 저장
    void insertWallet(Wallet wallet);

    // 사용자 ID로 지갑 정보 조회
    Optional<Wallet> findByUserId(String userId);

    // 지갑 주소로 지갑 정보 조회
    Optional<Wallet> findByWalletAddress(String walletAddress);

    // 지갑 정보 업데이트
    void updateWallet(Wallet wallet);

    // 지갑 정보 삭제
    void deleteWallet(String userId);

    // 토큰 잔액 업데이트
    void updateTokenBalance(@Param("userId") String userId, @Param("tokenBalance") int tokenBalance);
}