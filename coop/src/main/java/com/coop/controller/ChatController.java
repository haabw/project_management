package com.coop.controller;

import com.coop.dto.ChatDTO;
import com.coop.entity.UserEntity;
import com.coop.service.ChatService;
import com.coop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // ResponseEntity 사용
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller; // @Controller 사용
import org.springframework.web.bind.annotation.GetMapping; // GetMapping 사용
import org.springframework.web.bind.annotation.PathVariable; // PathVariable 사용
import org.springframework.web.bind.annotation.ResponseBody; // @ResponseBody 사용

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List; // List 사용

@Controller // WebSocket 메시지 처리와 REST API 엔드포인트를 함께 제공
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용 템플릿
    private final ChatService chatService;             // 채팅 관련 비즈니스 로직 처리
    private final UserService userService;             // 사용자 정보 조회용 서비스

    // 클라이언트에 표시될 날짜/시간 형식
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * WebSocket을 통해 클라이언트로부터 메시지를 수신하고 처리합니다.
     * 수신 경로: /app/chat.sendMessage/{projectId} (WebSocketConfig 설정과 일치)
     *
     * @param projectId 메시지가 속한 프로젝트 ID (경로 변수)
     * @param chatDTO   클라이언트가 보낸 메시지 데이터 (JSON -> ChatDTO 자동 변환)
     */
    @MessageMapping("/chat.sendMessage/{projectId}") // WebSocket 메시지 매핑
    public void sendMessage(@DestinationVariable String projectId, @Payload ChatDTO chatDTO) {
        // 1. 발신자 이름 설정 (DB 조회)
        try {
            Long userId = Long.parseLong(chatDTO.getSenderId());
            UserEntity sender = userService.findById(userId.intValue())
                    .orElseThrow(() -> new RuntimeException("메시지 발신자 정보를 찾을 수 없습니다. ID: " + userId));
            chatDTO.setSenderName(sender.getNickname());
        } catch (NumberFormatException e) {
            System.err.println("잘못된 사용자 ID 형식: " + chatDTO.getSenderId());
            chatDTO.setSenderName("Unknown User"); // 예외 발생 시 기본 이름
        } catch (Exception e) {
            System.err.println("발신자 정보 조회 오류: " + e.getMessage());
            chatDTO.setSenderName("Error User"); // 예외 발생 시 기본 이름
        }

        // 2. 서버 시간으로 타임스탬프 설정 (클라이언트 시간 불신)
        chatDTO.setTimestamp(LocalDateTime.now().format(formatter));

        // 3. 메시지 타입 및 프로젝트 ID 설정 (경로 변수 값 사용)
        chatDTO.setType(ChatDTO.MessageType.TALK);
        chatDTO.setProjectId(projectId);

        // 4. 메시지를 DB에 저장 (ChatService 호출)
        ChatDTO savedMessage = chatService.saveMessage(chatDTO);

        // 5. 해당 프로젝트 구독자들에게 메시지 브로드캐스트
        // 전송 경로: /topic/chat/project/{projectId} (WebSocketConfig 설정과 일치)
        String destination = "/topic/chat/project/" + projectId;
        messagingTemplate.convertAndSend(destination, savedMessage); // 저장된 메시지(ID, timestamp 포함) 전송
    }

    /**
     * WebSocket을 통해 사용자의 채팅방 입장을 처리합니다. (선택적 기능)
     * 수신 경로: /app/chat.addUser/{projectId}
     *
     * @param projectId 입장하는 프로젝트 ID
     * @param chatDTO   입장 사용자 정보 (senderId 포함)
     */
    @MessageMapping("/chat.addUser/{projectId}")
    public void addUser(@DestinationVariable String projectId, @Payload ChatDTO chatDTO) {
        // 1. 발신자 이름 조회 (DB 조회)
        String senderName = "Unknown User";
        try {
            Long userId = Long.parseLong(chatDTO.getSenderId());
            senderName = userService.findById(userId.intValue())
                    .map(UserEntity::getNickname) // Optional<UserEntity>에서 닉네임 추출
                    .orElse("Unknown User"); // 사용자가 없으면 기본 이름
        } catch(NumberFormatException e) {
            System.err.println("AddUser - 잘못된 사용자 ID 형식: " + chatDTO.getSenderId());
        } catch (Exception e) {
            System.err.println("AddUser - 발신자 정보 조회 오류: " + e.getMessage());
        }

        // 2. 입장 메시지 설정
        chatDTO.setSenderName(senderName);
        chatDTO.setMessage(senderName + " 님이 입장하셨습니다.");
        chatDTO.setType(ChatDTO.MessageType.ENTER);
        chatDTO.setTimestamp(LocalDateTime.now().format(formatter));
        chatDTO.setProjectId(projectId);

        // 3. 해당 프로젝트 구독자들에게 입장 메시지 브로드캐스트
        String destination = "/topic/chat/project/" + projectId;
        messagingTemplate.convertAndSend(destination, chatDTO);
    }

    /**
     * (REST API) 특정 프로젝트의 채팅 기록을 반환하는 엔드포인트.
     * 클라이언트(JavaScript)가 페이지 로드 시 호출합니다.
     * 경로: /api/chat/history/{projectId}
     *   
     @param //projectId 조회할 프로젝트 ID (URL 경로 변수)
     @return //해당 프로젝트의 채팅 기록 리스트 (JSON 형식으로 자동 변환)
  */
    @GetMapping("/api/chat/history/{projectId}")
    @ResponseBody // 이 메서드의 반환값을 HTTP 응답 본문(body)에 직접 쓰도록 지시 (JSON 반환)
    public ResponseEntity<List<ChatDTO>> getChatHistory(@PathVariable("projectId") Integer projectId) {
        try {
            List<ChatDTO> history = chatService.getChatHistory(projectId);
            return ResponseEntity.ok(history); // 성공 시 200 OK 와 함께 채팅 기록 반환
        } catch (EntityNotFoundException e) {
            // 프로젝트 ID가 잘못된 경우 등
            System.err.println("채팅 기록 조회 실패: " + e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found 응답
        } catch (Exception e) {
            System.err.println("채팅 기록 조회 중 서버 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().build(); // 500 Internal Server Error 응답
        }
    }
}