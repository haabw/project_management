package com.coop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private MessageType type; // 메시지 타입 (입장, 채팅, 퇴장 등)
    private String projectId;   // 메시지가 속한 프로젝트 ID
    private String senderId;    // 메시지 발신자 ID
    private String senderName;  // 메시지 발신자 이름
    private String message;     // 메시지 내용
    private String timestamp; // 메시지 발신 시간 (클라이언트 표기용 문자열)

    // 메시지 타입 정의 (Enum)
    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}