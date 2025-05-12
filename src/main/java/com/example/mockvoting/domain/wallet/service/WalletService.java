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
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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

    // Amoy 테스트넷에 맞는 가스 가격 설정
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(30_000_000_000L); // 30 Gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_000_000L);      // 1,000,000 gas

    // Web3j 초기화 메서드
    private Web3j getWeb3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    // 가스 제공자 설정 - Amoy 테스트넷에 맞게 조정
    private ContractGasProvider getGasProvider() {
        return new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
    }

    /**
     * 사용자 지갑 연결 (일반)
     * - 호환성을 위해 유지
     * - connectMetaMaskWallet 호출
     */
    @Transactional
    public WalletResponseDTO connectWallet(String userId, String walletAddress) {
        // 기존 메서드 호출 (컨트롤러 호환성 유지)
        return connectMetaMaskWallet(userId, walletAddress);
    }

    /**
     * 사용자 지갑 연결 (메타마스크 지갑 연결)
     * - 기존 지갑이 있으면 업데이트, 없으면 새로 생성
     * - 사용자당 하나의 지갑만 가질 수 있음
     */
    @Transactional
    public WalletResponseDTO connectMetaMaskWallet(String userId, String walletAddress) {
        // 입력 검증
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }
        if (walletAddress == null || walletAddress.isEmpty()) {
            log.error("유효하지 않은 지갑 주소: {}", userId);
            throw new CustomException("유효하지 않은 지갑 주소입니다.");
        }

        // 지갑 주소 형식 검증 (0x로 시작하는 42자 길이의 16진수 문자열)
        if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            log.error("잘못된 이더리움 지갑 주소 형식: {}", walletAddress);
            throw new CustomException("유효하지 않은 이더리움 지갑 주소 형식입니다.");
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
            wallet.setPrivateKey(""); // 메타마스크 지갑은 private key 저장 안함
            wallet.setWalletType("METAMASK");
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);

            log.info("기존 지갑을 메타마스크로 업데이트: userId={}, walletAddress={}, tokenBalance={}",
                    userId, walletAddress, wallet.getTokenBalance());
        } else {
            // 새 지갑 정보 저장
            isNewWallet = true;
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey("") // 메타마스크 지갑은 private key 저장 안함
                    .walletType("METAMASK")
                    .tokenBalance(0) // 초기 토큰은 0으로 설정 (토큰 발급은 별도 프로세스)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);
            log.info("새 메타마스크 지갑 연결 완료: userId={}, walletAddress={}", userId, walletAddress);
        }

        // 사용자가 아직 토큰을 받지 않았고, 메타마스크 지갑인 경우 블록체인에 토큰 발급 요청
        if (!user.isHasReceivedToken()) {
            try {
                // 블록체인에서 실제 잔액 조회 먼저 실행
                BigInteger currentBalance = getTokenBalanceFromBlockchain(walletAddress);
                int tokenBalance = currentBalance.divide(BigInteger.TEN.pow(18)).intValue();

                // 이미 토큰이 있는 경우 DB만 업데이트
                if (tokenBalance > 0) {
                    log.info("사용자 지갑에 이미 토큰이 있습니다: userId={}, walletAddress={}, tokenBalance={}",
                            userId, walletAddress, tokenBalance);

                    // 토큰 발급 상태 업데이트
                    userMapper.updateUserTokenStatus(userId, true);
                    wallet.setTokenBalance(tokenBalance);
                    walletMapper.updateTokenBalance(userId, tokenBalance);
                } else {
                    // 실제 블록체인에 토큰 발급 로직
                    boolean tokenIssued = issueTokenOnBlockchain(walletAddress);

                    if (tokenIssued) {
                        // 토큰 발급 성공 시 사용자 상태 업데이트
                        userMapper.updateUserTokenStatus(userId, true);

                        // 로컬 DB의 토큰 잔액도 업데이트 (UI 표시용)
                        wallet.setTokenBalance(10);
                        walletMapper.updateTokenBalance(userId, 10);

                        log.info("블록체인 토큰 발급 완료: userId={}, walletAddress={}", userId, walletAddress);
                    }
                }
            } catch (Exception e) {
                log.error("블록체인 토큰 발급 실패: {}", e.getMessage(), e);
                // 블록체인 토큰 발급 실패해도 지갑 연결은 유지
            }
        } else {
            // 이미 토큰을 받았으므로 블록체인에서 실제 잔액만 동기화
            try {
                BigInteger currentBalance = getTokenBalanceFromBlockchain(walletAddress);
                int tokenBalance = currentBalance.divide(BigInteger.TEN.pow(18)).intValue();

                // DB에 저장된 토큰 잔액과 블록체인 잔액이 다르면 동기화
                if (tokenBalance != wallet.getTokenBalance()) {
                    wallet.setTokenBalance(tokenBalance);
                    walletMapper.updateTokenBalance(userId, tokenBalance);
                    log.info("토큰 잔액 동기화 완료: userId={}, 이전 잔액={}, 현재 잔액={}",
                            userId, wallet.getTokenBalance(), tokenBalance);
                }
            } catch (Exception e) {
                log.warn("토큰 잔액 동기화 실패: {}", e.getMessage());
            }

            log.info("이미 토큰을 받은 사용자입니다: userId={}", userId);
        }

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType("METAMASK")
                .connected(true)
                .build();
    }

    /**
     * 새 지갑 생성 (시스템 내부 지갑)
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
            int originalBalance = wallet.getTokenBalance(); // 기존 잔액 저장

            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setWalletType("INTERNAL");
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateWallet(wallet);

            // 중요: tokenBalance는 업데이트하지 않음 (기존 값 유지)
            wallet.setTokenBalance(originalBalance);

            log.info("기존 지갑 정보 업데이트(시스템 지갑): userId={}, walletAddress={}, tokenBalance={}",
                    userId, walletAddress, wallet.getTokenBalance());
        } else {
            // 새 지갑 정보 저장
            isNewWallet = true;
            wallet = Wallet.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .privateKey(privateKey)
                    .walletType("INTERNAL")
                    .tokenBalance(0) // 초기 토큰은 0으로 설정 (토큰 발급은 별도 처리)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            walletMapper.insertWallet(wallet);
            log.info("새 시스템 지갑 생성 완료: userId={}, walletAddress={}", userId, walletAddress);
        }

        // 사용자가 아직 토큰을 받지 않았다면 토큰 발급
        if (!user.isHasReceivedToken()) {
            // 초기 토큰 10개 지급
            wallet.setTokenBalance(10);
            walletMapper.updateTokenBalance(userId, 10);

            // 토큰 발급 여부 업데이트
            userMapper.updateUserTokenStatus(userId, true);
            log.info("초기 토큰 발급 완료 (시스템 지갑): userId={}, tokenBalance=10", userId);
        } else {
            log.info("이미 토큰을 발급받은 사용자: userId={}, tokenBalance={}",
                    userId, wallet.getTokenBalance());
        }

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType("INTERNAL")
                .connected(true)
                .build();
    }

    /**
     * 초기 토큰 발급 - 호환성을 위한 메서드
     */
    @Transactional
    public WalletResponseDTO issueInitialToken(String userId, String walletAddress, String privateKey) {
        // 이 메서드는 createNewWallet 또는 connectMetaMaskWallet에서 이미 처리되고 있으므로
        // 여기서는 간단히 지갑 정보를 조회하여 반환

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            // 지갑이 없으면 새로 생성
            if (privateKey != null && !privateKey.isEmpty()) {
                // privateKey가 있으면 내부 지갑 생성
                return createNewWallet(userId, walletAddress, privateKey);
            } else {
                // privateKey가 없으면 메타마스크 연결
                return connectMetaMaskWallet(userId, walletAddress);
            }
        }

        // 이미 지갑이 있으면 해당 지갑 정보 반환
        Wallet wallet = walletOpt.get();
        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(wallet.getTokenBalance())
                .walletType(wallet.getWalletType())
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

        return walletOpt.map(wallet -> {
                    // 메타마스크 지갑인 경우 블록체인에서 실제 잔액 조회
                    if ("METAMASK".equals(wallet.getWalletType())) {
                        try {
                            // 실제 블록체인에서 토큰 잔액 조회 시도
                            BigInteger balance = getTokenBalanceFromBlockchain(wallet.getWalletAddress());
                            // Wei 단위를 토큰 단위로 변환 (10^18로 나눔)
                            int tokenBalance = balance.divide(BigInteger.TEN.pow(18)).intValue();

                            // DB에 저장된 잔액과 다르면 업데이트
                            if (tokenBalance != wallet.getTokenBalance()) {
                                walletMapper.updateTokenBalance(userId, tokenBalance);
                                wallet.setTokenBalance(tokenBalance);
                            }
                        } catch (Exception e) {
                            log.warn("블록체인 토큰 잔액 조회 실패, DB 값 사용: {}", e.getMessage());
                            // 블록체인 조회 실패 시 DB에 저장된 값 사용
                        }
                    }

                    log.info("지갑 상태 조회: userId={}, connected=true, tokenBalance={}, type={}",
                            userId, wallet.getTokenBalance(), wallet.getWalletType());

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
     * 토큰 잔액 조회
     */
    @Transactional(readOnly = true)
    public int getTokenBalance(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new CustomException("유효하지 않은 사용자 ID입니다.");
        }

        Optional<Wallet> walletOpt = walletMapper.findByUserId(userId);

        if (walletOpt.isEmpty()) {
            return 0;
        }

        Wallet wallet = walletOpt.get();

        // 메타마스크 지갑인 경우 블록체인에서 실제 잔액 조회 시도
        if ("METAMASK".equals(wallet.getWalletType())) {
            try {
                BigInteger balance = getTokenBalanceFromBlockchain(wallet.getWalletAddress());
                // Wei 단위를 토큰 단위로 변환 (10^18로 나눔)
                int tokenBalance = balance.divide(BigInteger.TEN.pow(18)).intValue();

                // DB에 저장된 잔액과 다르면 업데이트
                if (tokenBalance != wallet.getTokenBalance()) {
                    walletMapper.updateTokenBalance(userId, tokenBalance);
                    return tokenBalance;
                }
            } catch (Exception e) {
                log.warn("블록체인 토큰 잔액 조회 실패, DB 값 사용: {}", e.getMessage());
                // 블록체인 조회 실패 시 DB에 저장된 값 사용
            }
        }

        int balance = wallet.getTokenBalance();
        log.info("토큰 잔액 조회: userId={}, balance={}, type={}", userId, balance, wallet.getWalletType());
        return balance;
    }

    /**
     * 토큰 차감 (투표 참여 시)
     * - 메타마스크 지갑은 실제 블록체인에서 차감되므로 이 메소드는 호출되지 않음
     * - 내부 지갑만 이 메소드를 통해 차감
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

        // 메타마스크 지갑은 블록체인에서 직접 차감되므로 이 메소드를 통한 차감은 하지 않음
        if ("METAMASK".equals(wallet.getWalletType())) {
            log.warn("메타마스크 지갑은 블록체인에서 직접 차감됩니다: {}", userId);
            throw new CustomException("메타마스크 지갑은 직접 트랜잭션을 보내야 합니다.");
        }

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

        log.info("토큰 차감 완료 (시스템 지갑): userId={}, amount={}, 남은 토큰={}", userId, amount, newBalance);

        return WalletResponseDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .tokenBalance(newBalance)
                .walletType(wallet.getWalletType())
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

    /**
     * 블록체인에 토큰 발급 요청 (메타마스크 지갑용)
     */
    private boolean issueTokenOnBlockchain(String recipientAddress) {
        Web3j web3j = null;
        try {
            web3j = getWeb3j();
            Credentials adminCredentials = Credentials.create(adminPrivateKey);
            ContractGasProvider gasProvider = getGasProvider();

            // VotingToken 컨트랙트 로드 - Amoy 테스트넷 설정
            VotingToken votingToken = VotingToken.load(
                    tokenContractAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            // 이미 토큰을 받았는지 확인
            Boolean hasReceived = votingToken.hasReceivedInitialTokens(recipientAddress).send();
            if (hasReceived) {
                log.info("해당 지갑 주소는 이미 토큰을 받았습니다: {}", recipientAddress);
                return false;
            }

            // 토큰 발급 트랜잭션 실행
            TransactionReceipt receipt = votingToken.issueInitialTokens(recipientAddress).send();

            // 트랜잭션 성공 여부 확인
            if (!receipt.isStatusOK()) {
                log.error("토큰 발급 트랜잭션 실패: {}", receipt.getStatus());
                return false;
            }

            log.info("블록체인에 토큰 발급 성공: 주소={}, 트랜잭션 해시={}",
                    recipientAddress, receipt.getTransactionHash());
            return true;
        } catch (Exception e) {
            log.error("블록체인 토큰 발급 오류: {}", e.getMessage(), e);
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
            ContractGasProvider gasProvider = getGasProvider();

            // VotingToken 컨트랙트 로드
            VotingToken votingToken = VotingToken.load(
                    tokenContractAddress,
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            // 잔액 조회
            return votingToken.balanceOf(walletAddress).send();
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }

    /**
     * 블록체인 트랜잭션 검증
     * 투표 검증 시 사용
     */
    public boolean verifyTransaction(String txHash, String walletAddress, Integer candidateId) {
        Web3j web3j = null;
        try {
            web3j = getWeb3j();

            // 트랜잭션 영수증 가져오기
            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(txHash).send();

            if (!transactionReceipt.hasError() && transactionReceipt.getTransactionReceipt().isPresent()) {
                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();

                // 컨트랙트 주소 확인
                if (!receipt.getTo().equalsIgnoreCase(tokenContractAddress)) {
                    log.error("트랜잭션 대상이 VotingToken 컨트랙트가 아님: {}", receipt.getTo());
                    return false;
                }

                // 트랜잭션 발신자 확인
                if (!receipt.getFrom().equalsIgnoreCase(walletAddress)) {
                    log.error("트랜잭션 발신자가 일치하지 않음: {} != {}", receipt.getFrom(), walletAddress);
                    return false;
                }

                // 트랜잭션 상태 확인
                if (!receipt.isStatusOK()) {
                    log.error("트랜잭션이 성공적으로 처리되지 않음: {}", receipt.getStatus());
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

                // 이벤트 파라미터 검증 - 후보자 ID 확인
                for (VotingToken.VoteCastEventResponse event : events) {
                    if (event.voter.equalsIgnoreCase(walletAddress) &&
                            event.candidateId.intValue() == candidateId) {
                        log.info("트랜잭션 검증 성공: {}", txHash);
                        return true;
                    }
                }

                log.error("트랜잭션의 이벤트 파라미터가 일치하지 않음");
                return false;
            } else {
                log.error("트랜잭션 영수증을 가져올 수 없음: {}", txHash);
                return false;
            }
        } catch (Exception e) {
            log.error("트랜잭션 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
        } finally {
            if (web3j != null) {
                web3j.shutdown();
            }
        }
    }
}