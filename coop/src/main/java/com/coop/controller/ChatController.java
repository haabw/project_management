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
import org.springframework.ui.Model; // Model 임포트 추가
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam; // RequestParam 임포트 추가

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

    // --- chatPage 메소드 수정 ---
    @GetMapping("/chat/{projectId}")
    public String chatPage(@PathVariable Long projectId,
                           @RequestParam(name = "userId", required = true) Integer userId, // URL 파라미터 받기
                           @RequestParam(name = "username", required = true) String username, // URL 파라미터 받기
                           Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("userId", userId);       // 모델에 userId 추가
        model.addAttribute("username", username);   // 모델에 username 추가
        log.info("채팅 페이지 요청: projectId={}, userId={}, username={}", projectId, userId, username);
        return "chat";
    }
    // --- 수정 완료 ---


    @MessageMapping("/chat.sendMessage/{projectId}")
    public void sendMessage(@DestinationVariable Long projectId, @Payload ChatDTO chatDTO) {
        log.info("메시지 수신: projectId={}, senderId={}, message={}", projectId, chatDTO.getSenderId(), chatDTO.getMessage());
        //필요시 해당 부분 수정
        if (chatDTO.getProjectId() == null) {
            chatDTO.setProjectId(projectId);
        }
        // DTO 타입 설정은 ChatService에서 하므로 제거해도 무방
        // chatDTO.setType(ChatDTO.MessageType.TALK);

        try {
            ChatDTO savedMessageDTO = chatService.saveMessage(chatDTO);
            String destination = String.format("/topic/chat/project/%d", projectId);
            messagingTemplate.convertAndSend(destination, savedMessageDTO);
            log.info("메시지 전송 완료: destination={}, message={}", destination, savedMessageDTO.getMessage());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 (projectId: {}): {}", projectId, e.getMessage(), e);
        }
    }

    @GetMapping("/api/chat/history/{projectId}")
    @ResponseBody
    public ResponseEntity<List<ChatDTO>> getChatHistory(@PathVariable Long projectId) {
        log.info("채팅 기록 요청: projectId={}", projectId);
        try {
            List<ChatDTO> history = chatService.getChatHistory(projectId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("채팅 기록 조회 중 오류 발생 (projectId: {}): {}", projectId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}