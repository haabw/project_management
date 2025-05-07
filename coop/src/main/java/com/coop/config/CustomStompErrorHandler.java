package com.coop.config; // 현재 프로젝트의 config 패키지

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

// import java.nio.charset.StandardCharsets;

@Component
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomStompErrorHandler.class);

    public CustomStompErrorHandler() {
        super();
        logger.info("CustomStompErrorHandler initialized.");
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        logger.info("CustomStompErrorHandler: handleClientMessageProcessingError CALLED. Exception: {}, ClientMessageHeaders: {}", ex.getClass().getName(), clientMessage != null ? clientMessage.getHeaders() : "null"); // 로그 추가!
        Throwable cause = ex.getCause();

        if (ex instanceof AccessDeniedException || (cause != null && cause instanceof AccessDeniedException)) {
            AccessDeniedException accessDeniedException = (ex instanceof AccessDeniedException) ?
                    (AccessDeniedException) ex : (AccessDeniedException) cause;

            String errorMessage = accessDeniedException.getMessage();
            if (errorMessage == null) {
                errorMessage = "접근이 거부되었습니다.";
            }
            logger.warn("CustomStompErrorHandler: AccessDeniedException caught. Original message: {}", errorMessage);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
            accessor.setMessage(errorMessage);
            accessor.setLeaveMutable(true);

            return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        }

        logger.error("CustomStompErrorHandler: Unhandled client message processing error. ClientMessage: {}, Exception: ", clientMessage, ex);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        // 클라이언트에게 너무 상세한 내부 오류 메시지 노출은 피하는 것이 좋습니다.
        // ex.getMessage()는 개발 로깅에는 유용하지만, 사용자에게 직접 보여주기엔 부적절할 수 있습니다.
        String userFriendlyErrorMessage = "서버 내부 오류로 메시지 처리에 실패했습니다. 잠시 후 다시 시도해주세요.";
        // if (ex.getMessage() != null) {
        //     userFriendlyErrorMessage += " (Debug: " + ex.getMessage() + ")"; // 디버깅 시에만 포함 고려
        // }
        accessor.setMessage(userFriendlyErrorMessage);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // 아래 메소드는 컴파일 오류를 유발하므로 일단 주석 처리 또는 삭제합니다.
    /*
    @Override
    protected Message<byte[]> handleInternalKiError(Message<byte[]> clientMessage, Throwable ex, StompHeaderAccessor clientHeaderAccessor) {
        // Spring 버전에 따라 이 메소드 시그니처가 StompSubProtocolErrorHandler에 없을 수 있습니다.
        // (예: handleInternalError 로 변경되었거나 파라미터가 다를 수 있음)
        logger.error("CustomStompErrorHandler: handleInternalKiError (or similar) called.", ex);
        // return super.handleInternalKiError(clientMessage, ex, clientHeaderAccessor); // 이 호출도 오류 유발 가능
        
        // 이 메소드가 꼭 필요하다면, 현재 Spring 버전의 StompSubProtocolErrorHandler 소스를 확인하여
        // 정확한 오버라이드 대상 메소드를 찾아야 합니다.
        // 지금은 주된 문제 해결을 위해 handleClientMessageProcessingError에 집중합니다.
        // 임시로 null이나 기본 에러 메시지를 반환할 수도 있습니다.
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage("서버 내부 처리 중 예기치 않은 오류가 발생했습니다.");
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
    */
}