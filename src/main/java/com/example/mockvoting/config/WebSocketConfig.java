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
        registry.addEndpoint("/wss")
                .setAllowedOrigins("https://mock-election-frontend.web.app") // '*' 대신 특정 URL
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

}