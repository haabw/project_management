package com.coop.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Logger 추가
import org.slf4j.LoggerFactory; // Logger 추가
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler; // 추가

@Configuration
@EnableWebSocketMessageBroker
// @RequiredArgsConstructor // customStompErrorHandler 때문에 final 필드 생성자가 필요하므로 유지 또는 수동 생성자 추가
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class); // 로거 추가

    private final SubscriptionAuthInterceptor subscriptionAuthInterceptor;
    private final StompSubProtocolErrorHandler customStompErrorHandler; // 명시적으로 주입

    // Lombok @RequiredArgsConstructor 대신 수동 생성자 (필요시)
    public WebSocketConfig(SubscriptionAuthInterceptor subscriptionAuthInterceptor,
                           StompSubProtocolErrorHandler customStompErrorHandler) {
        this.subscriptionAuthInterceptor = subscriptionAuthInterceptor;
        this.customStompErrorHandler = customStompErrorHandler;
        if (this.customStompErrorHandler != null) {
            logger.info("WebSocketConfig: CustomStompErrorHandler 주입 성공! 클래스: {}", this.customStompErrorHandler.getClass().getName());
        } else {
            logger.error("WebSocketConfig: CustomStompErrorHandler 주입 실패!");
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // 이전 시도에서 에러 핸들러를 여기에 등록하려 했으나, Spring이 자동으로 Bean을 찾도록 합니다.
        // 단, customStompErrorHandler가 제대로 Bean으로 등록되어 주입되는지 위 생성자 로그로 확인합니다.
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(subscriptionAuthInterceptor);
    }
}