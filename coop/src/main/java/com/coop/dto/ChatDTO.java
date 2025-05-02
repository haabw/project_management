package com.coop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDTO {

    private MessageType type;
    private Long projectId;

    // private Long senderId; // 변경 전
    private Integer senderId; // 변경 후 (UserEntity.id가 int이므로 Integer 사용)

    private String senderName;
    private String message;
    private String timestamp;

    public enum MessageType {
        ENTER,
        TALK
    }
}