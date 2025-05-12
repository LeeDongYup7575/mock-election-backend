package com.example.mockvoting.domain.wallet.service;

import com.example.mockvoting.domain.wallet.contract.VotingToken;
import com.example.mockvoting.domain.wallet.dto.WalletResponseDTO;
import com.example.mockvoting.domain.wallet.entity.Wallet;
import com.example.mockvoting.domain.wallet.mapper.WalletMapper;
import com.example.mockvoting.domain.user.entity.User;
import com.example.mockvoting.domain.user.mapper.UserMapper;
import com.example.mockvoting.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;
    private final UserMapper userMapper;

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.token-contract-address}")
    private String tokenContractAddress;

    @Value("${blockchain.admin-private-key}")
    private String adminPrivateKey;

    @Value("${blockchain.chain-id}")
    private BigInteger chainId;

    // 가스 설정 (테스트넷용으로 낮춤)
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L); // 10 Gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(300_000L);

    // 초기 토큰 양 (UI 표시)
    private static final int INITIAL_TOKEN_AMOUNT = 1;

    private Web3j getWeb3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    private ContractGasProvider getGasProvider() {
        return new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
    }

    /**
     * 사용자 지갑 연결 (일반) - 호환성을 위해 유지
     */
    @Transactional
    public WalletResponseDTO connectWallet(String userId, String walletAddress) {
        return connectMetaMaskWallet(userId, walletAddress);
    }

    /**
     * 메타마스크 지갑 연결 - 간소화된 버전
     */
    @Transactional
    public WalletResponseDTO connectMetaMaskWallet(String userId, String walletAddress) {
        log.info("=== 메타마스크 지갑 연결 시작 ===");
        log.info("userId: {}, walletAddress: {}", userId, walletAddress);

        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 사용자 확인
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));
        log.info("사용자 확인 완료: {}", user.getUserId());
        log.info("사용자 토큰 발급 여부: {}", user.isHasReceivedToken());

        // 지갑 등록/업데이트
        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);
        Wallet wallet;

        if (existingWallet.isPresent()) {
            // 기존 지갑 업데이트
            wallet = existingWallet.get();
            wallet.setWalletAddress(walletAddress);
            wallet.setWalletType("METAMASK");
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);
            log.info("기존 지갑 업데이트 완료");
        } else {
            // 새 지갑 등록
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey("")
                    .walletType("METAMASK")
                    .tokenBalance(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            walletMapper.insertWallet(wallet);
            log.info("새 지갑 등록 완료");
        }

        // 토큰 처리
        try {
            // 1. 블록체인에서 현재 잔액 확인
            BigInteger currentBalance = getTokenBalanceFromBlockchain(walletAddress);
            int tokenBalance = currentBalance.divide(BigInteger.TEN.pow(18)).intValue();
            log.info("블록체인 토큰 잔액: {} (wei: {})", tokenBalance, currentBalance);

            // 2. 토큰이 없으면 발급 시도
            if (tokenBalance == 0) {
                log.info("토큰이 없으므로 발급을 시도합니다.");

                try {
                    // Admin 잔액 확인
                    checkAdminBalance();

                    // 블록체인에 토큰 발급
                    boolean tokenIssued = issueTokenOnBlockchain(walletAddress);

                    if (tokenIssued) {
                        tokenBalance = INITIAL_TOKEN_AMOUNT;
                        log.info("블록체인 토큰 발급 성공!");
                    } else {
                        log.warn("블록체인 토큰 발급 실패, DB에서만 관리");
                        tokenBalance = INITIAL_TOKEN_AMOUNT;
                    }
                } catch (Exception e) {
                    log.error("블록체인 토큰 발급 중 오류, DB에서만 관리: {}", e.getMessage());
                    tokenBalance = INITIAL_TOKEN_AMOUNT;
                }

                // 사용자 토큰 발급 상태 업데이트
                if (!user.isHasReceivedToken()) {
                    userMapper.updateUserTokenStatus(userId, true);
                    log.info("사용자 토큰 발급 상태 업데이트 완료");
                }
            }

            // 3. DB 토큰 잔액 업데이트
            wallet.setTokenBalance(tokenBalance);
            walletMapper.updateTokenBalance(userId, tokenBalance);
            log.info("최종 토큰 잔액: {}", tokenBalance);

        } catch (Exception e) {
            log.error("토큰 처리 중 최종 오류: {}", e.getMessage(), e);
            // 오류 발생 시에도 기본 토큰 제공
            wallet.setTokenBalance(INITIAL_TOKEN_AMOUNT);
            walletMapper.updateTokenBalance(userId, INITIAL_TOKEN_AMOUNT);

            if (!user.isHasReceivedToken()) {
                userMapper.updateUserTokenStatus(userId, true);
            }
        }

        log.info("=== 메타마스크 지갑 연결 완료 ===");

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType("METAMASK")
                .connected(true)
                .build();
    }

    /**
     * 내부 지갑 생성 - 간소화된 버전
     */
    @Transactional
    public WalletResponseDTO createNewWallet(String userId, String walletAddress, String privateKey) {
        log.info("=== 내부 지갑 생성 시작 ===");
        log.info("userId: {}", userId);

        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        Optional<Wallet> existingWallet = walletMapper.findByUserId(userId);
        Wallet wallet;

        if (existingWallet.isPresent()) {
            // 기존 지갑 업데이트
            wallet = existingWallet.get();
            int originalBalance = wallet.getTokenBalance();

            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setWalletType("INTERNAL");
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);

            wallet.setTokenBalance(originalBalance);
            log.info("기존 지갑 업데이트 완료, 토큰 잔액 유지: {}", originalBalance);
        } else {
            // 새 지갑 생성
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey(privateKey)
                    .walletType("INTERNAL")
                    .tokenBalance(INITIAL_TOKEN_AMOUNT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);

            // 첫 지갑 생성 시 토큰 발급 상태 업데이트
            if (!user.isHasReceivedToken()) {
                userMapper.updateUserTokenStatus(userId, true);
                log.info("초기 토큰 발급 완료: {} 토큰", INITIAL_TOKEN_AMOUNT);
            }
        }

        log.info("=== 내부 지갑 생성 완료 ===");

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType("INTERNAL")
                .connected(true)
                .build();
    }

    /**
     * 지갑 상태 조회 - 토큰 발급 로직 포함
     */
    @Transactional
    public WalletResponseDTO getWalletStatus(String userId) {
        log.info("=== 지갑 상태 조회 시작: {} ===", userId);

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        return walletOpt.map(wallet -> {
                    log.info("지갑 발견: type={}, balance={}", wallet.getWalletType(), wallet.getTokenBalance());

                    // 메타마스크 지갑이고 토큰이 0이면 발급 시도
                    if ("METAMASK".equals(wallet.getWalletType()) && wallet.getTokenBalance() == 0) {
                        log.info("토큰이 0인 메타마스크 지갑 감지, 토큰 발급 시도");

                        try {
                            // 블록체인에서 실제 잔액 확인
                            BigInteger balance = getTokenBalanceFromBlockchain(wallet.getWalletAddress());
                            int tokenBalance = balance.divide(BigInteger.TEN.pow(18)).intValue();

                            if (tokenBalance == 0) {
                                // 토큰 발급 시도
                                boolean issued = issueTokenOnBlockchain(wallet.getWalletAddress());
                                if (issued) {
                                    tokenBalance = INITIAL_TOKEN_AMOUNT;
                                    walletMapper.updateTokenBalance(userId, tokenBalance);
                                    userMapper.updateUserTokenStatus(userId, true);
                                    wallet.setTokenBalance(tokenBalance);
                                    log.info("토큰 발급 성공!");
                                } else {
                                    // 블록체인 발급 실패 시 DB만 업데이트
                                    tokenBalance = INITIAL_TOKEN_AMOUNT;
                                    walletMapper.updateTokenBalance(userId, tokenBalance);
                                    userMapper.updateUserTokenStatus(userId, true);
                                    wallet.setTokenBalance(tokenBalance);
                                    log.info("블록체인 발급 실패, DB만 업데이트");
                                }
                            }
                        } catch (Exception e) {
                            log.error("토큰 발급 중 오류: {}", e.getMessage());
                            // 오류 시에도 DB는 업데이트
                            wallet.setTokenBalance(INITIAL_TOKEN_AMOUNT);
                            walletMapper.updateTokenBalance(userId, INITIAL_TOKEN_AMOUNT);
                            userMapper.updateUserTokenStatus(userId, true);
                        }
                    }

                    return WalletResponseDTO.builder()
                            .walletAddress(wallet.getWalletAddress())
                            .tokenBalance(wallet.getTokenBalance())
                            .walletType(wallet.getWalletType())
                            .connected(true)
                            .build();
                })
                .orElse(WalletResponseDTO.builder()
                        .connected(false)
                        .build());
    }

    /**
     * Admin 잔액 확인
     */
    private void checkAdminBalance() {
        Web3j web3j = null;
        try {
            web3j = getWeb3j();
            Credentials adminCredentials = Credentials.create(adminPrivateKey);
            String adminAddress = adminCredentials.getAddress();

            EthGetBalance balance = web3j.ethGetBalance(
                    adminAddress, DefaultBlockParameterName.LATEST).send();

            BigInteger weiBalance = balance.getBalance();
            BigDecimal etherBalance = Convert.fromWei(
                    new BigDecimal(weiBalance), Convert.Unit.ETHER);

            log.info("Admin 계정 주소: {}", adminAddress);
            log.info("Admin 계정 잔액: {} ETH", etherBalance);

            // 최소 가스비 확인
            BigInteger requiredGas = GAS_PRICE.multiply(GAS_LIMIT);
            if (weiBalance.compareTo(requiredGas) < 0) {
                log.error("Admin 계정 가스비 부족! 필요: {} wei, 현재: {} wei",
                        requiredGas, weiBalance);
            }
        } catch (Exception e) {
            log.error("Admin 잔액 확인 실패: {}", e.getMessage());
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }

    /**
     * 블록체인 토큰 발급 - 간소화 버전
     */
    private boolean issueTokenOnBlockchain(String recipientAddress) {
        log.info("블록체인 토큰 발급 시작: {}", recipientAddress);

        Web3j web3j = null;
        try {
            web3j = getWeb3j();
            Credentials adminCredentials = Credentials.create(adminPrivateKey);

            // 컨트랙트 로드
            VotingToken votingToken = VotingToken.load(
                    tokenContractAddress,
                    web3j,
                    adminCredentials,
                    getGasProvider()
            );

            // 이미 토큰을 받았는지 확인
            Boolean hasReceived = votingToken.hasReceivedInitialTokens(recipientAddress).send();
            if (hasReceived) {
                log.info("이미 토큰을 받은 주소입니다.");
                return true;
            }

            // 토큰 발급 트랜잭션
            log.info("토큰 발급 트랜잭션 전송...");
            TransactionReceipt receipt = votingToken.issueInitialTokens(recipientAddress).send();

            log.info("트랜잭션 해시: {}", receipt.getTransactionHash());
            log.info("트랜잭션 상태: {}", receipt.getStatus());

            return receipt.isStatusOK();

        } catch (Exception e) {
            log.error("블록체인 토큰 발급 오류: {}", e.getMessage());
            return false;
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }

    /**
     * 블록체인에서 토큰 잔액 조회
     */
    public BigInteger getTokenBalanceFromBlockchain(String walletAddress) throws Exception {
        Web3j web3j = null;
        try {
            web3j = getWeb3j();
            Credentials adminCredentials = Credentials.create(adminPrivateKey);

            VotingToken votingToken = VotingToken.load(
                    tokenContractAddress,
                    web3j,
                    adminCredentials,
                    getGasProvider()
            );

            BigInteger balance = votingToken.balanceOf(walletAddress).send();
            log.info("블록체인 토큰 잔액 조회: {} wei", balance);
            return balance;
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }

    /**
     * 토큰 잔액 조회
     */
    @Transactional(readOnly = true)
    public int getTokenBalance(String userId) {
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            return 0;
        }
        return walletOpt.get().getTokenBalance();
    }

    /**
     * 토큰 차감 (내부 지갑용)
     */
    @Transactional
    public WalletResponseDTO deductToken(String userId, int amount) {
        Wallet wallet = walletMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException("연결된 지갑이 없습니다."));

        if ("METAMASK".equals(wallet.getWalletType())) {
            throw new CustomException("메타마스크 지갑은 직접 트랜잭션을 보내야 합니다.");
        }

        if (wallet.getTokenBalance() < amount) {
            throw new CustomException("토큰 잔액이 부족합니다.");
        }

        int newBalance = wallet.getTokenBalance() - amount;
        wallet.setTokenBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateTokenBalance(userId, newBalance);

        log.info("토큰 차감 완료: userId={}, amount={}, 남은 토큰={}", userId, amount, newBalance);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(newBalance)
                .walletType(wallet.getWalletType())
                .connected(true)
                .build();
    }

    /**
     * 지갑 연결 해제
     */
    @Transactional
    public void disconnectWallet(String userId) {
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        if (walletOpt.isPresent()) {
            walletMapper.deleteWallet(userId);
            log.info("지갑 연결 해제 완료: userId={}", userId);
        }
    }

    /**
     * 초기 토큰 발급 - 호환성을 위한 메서드
     */
    @Transactional
    public WalletResponseDTO issueInitialToken(String userId, String walletAddress, String privateKey) {
        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            if (privateKey != null && !privateKey.isEmpty()) {
                return createNewWallet(userId, walletAddress, privateKey);
            } else {
                return connectMetaMaskWallet(userId, walletAddress);
            }
        }

        Wallet wallet = walletOpt.get();
        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType(wallet.getWalletType())
                .connected(true)
                .build();
    }

    /**
     * 트랜잭션 검증 (투표용)
     */
    public boolean verifyTransaction(String txHash, String walletAddress, Integer candidateId) {
        Web3j web3j = null;
        try {
            web3j = getWeb3j();

            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(txHash).send();

            if (!transactionReceipt.hasError() && transactionReceipt.getTransactionReceipt().isPresent()) {
                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();

                // 컨트랙트 주소 확인
                if (!receipt.getTo().equalsIgnoreCase(tokenContractAddress)) {
                    log.error("트랜잭션 대상이 VotingToken 컨트랙트가 아님");
                    return false;
                }

                // 트랜잭션 발신자 확인
                if (!receipt.getFrom().equalsIgnoreCase(walletAddress)) {
                    log.error("트랜잭션 발신자가 일치하지 않음");
                    return false;
                }

                // 트랜잭션 상태 확인
                if (!receipt.isStatusOK()) {
                    log.error("트랜잭션이 실패함");
                    return false;
                }

                // VotingToken 컨트랙트 로드하여 이벤트 확인
                Credentials adminCredentials = Credentials.create(adminPrivateKey);
                VotingToken votingToken = VotingToken.load(
                        tokenContractAddress,
                        web3j,
                        adminCredentials,
                        getGasProvider()
                );

                // VoteCast 이벤트 검증
                List<VotingToken.VoteCastEventResponse> events = votingToken.getVoteCastEvents(receipt);

                if (events.isEmpty()) {
                    log.error("트랜잭션에서 VoteCast 이벤트를 찾을 수 없음");
                    return false;
                }

                // 이벤트 파라미터 검증
                for (VotingToken.VoteCastEventResponse event : events) {
                    if (event.voter.equalsIgnoreCase(walletAddress) &&
                            event.candidateId.intValue() == candidateId) {
                        log.info("트랜잭션 검증 성공");
                        return true;
                    }
                }

                log.error("트랜잭션의 이벤트 파라미터가 일치하지 않음");
                return false;
            }

            return false;
        } catch (Exception e) {
            log.error("트랜잭션 검증 오류: {}", e.getMessage());
            return false;
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }
}