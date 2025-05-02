package com.coop.controller;

import com.coop.dto.ChatDTO;
import com.coop.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Model 임포트
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam; // RequestParam 임포트

import java.util.List;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public ChatController(ChatService chatService, SimpMessageSendingOperations messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 채팅 페이지를 렌더링합니다.
     * URL 경로에서 projectId를, 쿼리 파라미터에서 userId와 username을 받아 모델에 추가합니다.
     *
     * @param projectId  URL 경로 변수로 받는 프로젝트 ID
     * @param userId     쿼리 파라미터로 받는 사용자 ID
     * @param username   쿼리 파라미터로 받는 사용자 이름
     * @param model      Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return 채팅 페이지 템플릿 이름 ("chat")
     */
    @GetMapping("/chat/{projectId}")
    public String chatPage(@PathVariable Long projectId,
                           @RequestParam(name = "userId", required = true) Integer userId,
                           @RequestParam(name = "username", required = true) String username,
                           Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("userId", userId);
        model.addAttribute("username", username);
        log.info("채팅 페이지 요청: projectId={}, userId={}, username={}", projectId, userId, username);
        return "chat"; // templates/chat.html 렌더링
    }

    /**
     * 클라이언트로부터 채팅 메시지를 받아 처리하고, 해당 프로젝트 토픽으로 브로드캐스트합니다.
     *
     * @param projectId     메시지가 속한 프로젝트 ID (URL 경로 변수)
     * @param chatDTO       클라이언트로부터 받은 채팅 메시지 데이터 (STOMP 페이로드)
     */
    @MessageMapping("/chat.sendMessage/{projectId}")
    public void sendMessage(@DestinationVariable Long projectId, @Payload ChatDTO chatDTO) {
        log.info("메시지 수신: projectId={}, senderId={}, message={}", projectId, chatDTO.getSenderId(), chatDTO.getMessage());

        // chatDTO에 projectId가 설정되지 않았으면 URL 경로 변수 값으로 설정
        if (chatDTO.getProjectId() == null) {
            chatDTO.setProjectId(projectId);
        }

        try {
            // 채팅 메시지를 서비스 계층을 통해 저장하고, 저장된 메시지 DTO를 받음
            ChatDTO savedMessageDTO = chatService.saveMessage(chatDTO);

            // 해당 프로젝트의 STOMP 토픽 경로 생성
            String destination = String.format("/topic/chat/project/%d", projectId);

            // 메시지 템플릿을 사용하여 해당 토픽을 구독하는 모든 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend(destination, savedMessageDTO);
            log.info("메시지 전송 완료: destination={}, message={}", destination, savedMessageDTO.getMessage());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 (projectId: {}): {}", projectId, e.getMessage(), e);
            // 필요하다면, 오류 메시지를 특정 사용자 또는 관리자에게 보내는 로직 추가 가능
        }
    }

    /**
     * 특정 프로젝트의 채팅 기록을 조회하는 API 엔드포인트입니다.
     *
     * @param projectId 조회할 프로젝트 ID (URL 경로 변수)
     * @return 성공 시 채팅 기록 리스트(List<ChatDTO>)와 HTTP 200 OK, 실패 시 HTTP 500 Internal Server Error
     */
    @GetMapping("/api/chat/history/{projectId}")
    @ResponseBody // 이 메서드의 반환값을 HTTP 응답 본문으로 직접 사용하도록 지정
    public ResponseEntity<List<ChatDTO>> getChatHistory(@PathVariable Long projectId) {
        log.info("채팅 기록 요청: projectId={}", projectId);
        try {
            // 서비스 계층을 통해 채팅 기록 조회
            List<ChatDTO> history = chatService.getChatHistory(projectId);
            // 조회된 기록과 함께 HTTP 200 OK 응답 반환
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("채팅 기록 조회 중 오류 발생 (projectId: {}): {}", projectId, e.getMessage(), e);
            // 오류 발생 시 HTTP 500 Internal Server Error 응답 반환
            return ResponseEntity.internalServerError().build();
        }
    }
}