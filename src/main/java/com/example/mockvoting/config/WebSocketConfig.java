package com.example.mockvoting.config;

import com.example.mockvoting.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${front.url}")
    private String frontUrl;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { // 메시지를 전달하는 중개자 역할
        // 메시지 구독 요청 prefix (client가 구독할 수 있는 topic의 prefix)
        registry.enableSimpleBroker("/topic");

        // 메시지 발행 요청 prefix (client가 메시지를 발행할 때 사용할 prefix)
        registry.setApplicationDestinationPrefixes("/app");

        System.out.println("WebSocket 메시지 브로커 설정 완료: /topic, /app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Setting up STOMP endpoints with allowed origin: {}", frontUrl);
        // WebSocket 연결을 위한 엔드포인트 설정
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // '*' 대신 특정 URL
                .withSockJS();
    }

    // 추후 삭제 :: WebSocket 디버깅을 위한 핸들러 인터셉터 추가
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    System.out.println("WebSocket Message Type: " + accessor.getCommand());
                    if (accessor.getCommand() == StompCommand.SEND) {
                        System.out.println("WebSocket 메시지 전송: " + message.getPayload());
                        System.out.println("WebSocket 목적지: " + accessor.getDestination());
                    }
                }

                return message;
            }
        });
    }


//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    // 인증 헤더에서 JWT 토큰 추출
//                    List<String> authorization = accessor.getNativeHeader("Authorization");
//
//                    if (authorization != null && !authorization.isEmpty()) {
//                        String token = authorization.get(0);
//                        if (token != null && token.startsWith("Bearer ")) {
//                            token = token.substring(7);
//
//                            // JWT 토큰 검증
//                            try {
//                                if (jwtUtil.validateToken(token)) {
//                                    String userId = jwtUtil.getUserIdFromToken(token);
//                                    String role = jwtUtil.getRoleFromToken(token);
//
//                                    // 인증 정보 생성 및 설정
//                                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                                            userId,
//                                            null,
//                                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
//                                    );
//
//                                    SecurityContextHolder.getContext().setAuthentication(auth);
//                                    accessor.setUser(auth);
//
//                                    // 세션에 토큰 정보 저장
//                                    accessor.getSessionAttributes().put("userId", userId);
//                                    accessor.getSessionAttributes().put("role", role);
//                                }
//                            } catch (Exception e) {
//                                System.err.println("WebSocket JWT 검증 오류: " + e.getMessage());
//                            }
//                        }
//                    } else {
//                        // 토큰이 없어도 연결은 허용 (개발 중이므로)
//                        System.out.println("WebSocket 연결 시 인증 토큰이 없습니다.");
//                    }
//                }
//
//                return message;
//            }
//        });
//    }


}