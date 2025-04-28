package com.coop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 및 STOMP 메시징 설정을 위한 클래스입니다.
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 기능을 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 관련 설정을 구성합니다.
     * @param config MessageBrokerRegistry 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 경로(topic)의 prefix를 설정합니다.
        // "/topic"으로 시작하는 경로로 메시지를 보내면, 해당 경로를 구독하는 클라이언트에게 메시지가 전달됩니다.
        config.enableSimpleBroker("/topic");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 경로의 prefix를 설정합니다.
        // "/app"으로 시작하는 경로로 메시지를 보내면 @MessageMapping 어노테이션이 붙은 메서드로 라우팅됩니다.
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 프로토콜을 사용할 WebSocket 엔드포인트를 등록합니다.
     * @param registry StompEndpointRegistry 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트 경로를 설정합니다.
        // "/ws" 경로로 SockJS 연결을 허용합니다.
        // SockJS는 WebSocket을 지원하지 않는 브라우저에서도 유사한 경험을 제공하기 위한 fallback 옵션입니다.
        registry.addEndpoint("/ws").withSockJS();
    }
}
