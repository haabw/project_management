package com.coop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        // "/topic"으로 시작하는 경로를 구독하는 클라이언트에게 메시지 브로드캐스트
        config.enableSimpleBroker("/topic");
        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 Prefix 설정 ("/app"으로 시작)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 엔드포인트 설정
        // "/ws" 경로로 WebSocket 또는 SockJS 클라이언트의 연결 허용
        // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션 제공
        registry.addEndpoint("/ws").withSockJS();
    }
}