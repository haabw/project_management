package com.coop.controller;

import com.coop.dto.ChatDTO;
import com.coop.entity.ProjectEntity; // ProjectEntity 임포트
import com.coop.entity.ProjectMemberEntity; // ProjectMemberEntity 임포트
import com.coop.entity.UserEntity;
import com.coop.repository.ProjectMemberRepository; // ProjectMemberRepository 임포트
import com.coop.repository.ProjectRepository; // ProjectRepository 임포트
import com.coop.service.ChatService;
import com.coop.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException 임포트
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 현재 사용자 정보 가져오기 위해
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections; // Collections 임포트
import java.util.List;

import com.coop.entity.UserEntity;
import com.coop.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 예외 임포트
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 사용자가 특정 프로젝트의 승인된 멤버인지 확인합니다.
     * @param userId 확인할 사용자 ID
     * @param projectId 확인할 프로젝트 ID
     * @return 멤버이면 true, 아니면 false
     */
    private boolean isUserApprovedMemberOfProject(String userIdStr, String projectIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            Integer projectId = Integer.parseInt(projectIdStr);

            UserEntity user = userService.findById(userId.intValue())
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

            return projectMemberRepository.existsByUserAndProjectAndStatus(
                    user, project, ProjectMemberEntity.ProjectStatus.APPROVED);
        } catch (NumberFormatException | EntityNotFoundException e) {
            System.err.println("프로젝트 멤버 확인 중 오류: " + e.getMessage());
            return false;
        }
    }

    @MessageMapping("/chat.sendMessage/{projectId}")
    public void sendMessage(@DestinationVariable String projectId, @Payload ChatDTO chatDTO) {
        // 0. 사용자 권한 확인 (프로젝트 멤버인지)
        if (!isUserApprovedMemberOfProject(chatDTO.getSenderId(), projectId)) {
            System.err.println("권한 없음: 사용자 " + chatDTO.getSenderId() + "는 프로젝트 " + projectId + "의 멤버가 아닙니다. (sendMessage)");
            // 여기에 특정 사용자에게 오류 메시지를 보내는 로직을 추가할 수 있으나, STOMP에서는 복잡할 수 있습니다.
            // 현재는 단순히 메시지 처리를 중단합니다.
            return;
        }

        // 1. 발신자 이름 설정
        try {
            Long userId = Long.parseLong(chatDTO.getSenderId());
            UserEntity sender = userService.findById(userId.intValue())
                    .orElseThrow(() -> new RuntimeException("메시지 발신자 정보를 찾을 수 없습니다. ID: " + userId));
            chatDTO.setSenderName(sender.getNickname());
        } catch (NumberFormatException e) {
            System.err.println("잘못된 사용자 ID 형식: " + chatDTO.getSenderId());
            chatDTO.setSenderName("Unknown User");
        } catch (Exception e) {
            System.err.println("발신자 정보 조회 오류: " + e.getMessage());
            chatDTO.setSenderName("Error User");
        }

        chatDTO.setTimestamp(LocalDateTime.now().format(formatter));
        chatDTO.setType(ChatDTO.MessageType.TALK);
        chatDTO.setProjectId(projectId);

        try {
            ChatDTO savedMessage = chatService.saveMessage(chatDTO); // ChatService의 saveMessage는 이미 내부에서 권한 검사를 수행 (선택적 중복 검사)
            String destination = "/topic/chat/project/" + projectId;
            messagingTemplate.convertAndSend(destination, savedMessage);
        } catch (AccessDeniedException e) {
            System.err.println("메시지 저장 실패 (권한 없음): " + e.getMessage());
            // 이 경우에도 특정 사용자에게 오류를 보내는 것을 고려할 수 있습니다.
        } catch (Exception e) {
            System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.addUser/{projectId}")
    public void addUser(@DestinationVariable String projectId, @Payload ChatDTO chatDTO) {
        // 0. 사용자 권한 확인 (프로젝트 멤버인지)
        if (!isUserApprovedMemberOfProject(chatDTO.getSenderId(), projectId)) {
            System.err.println("권한 없음: 사용자 " + chatDTO.getSenderId() + "는 프로젝트 " + projectId + "의 멤버가 아닙니다. (addUser)");
            return; // 멤버가 아니면 입장 메시지 처리 안 함
        }

        // 1. 발신자 이름 조회
        String senderName = "Unknown User";
        try {
            Long userId = Long.parseLong(chatDTO.getSenderId());
            senderName = userService.findById(userId.intValue())
                    .map(UserEntity::getNickname)
                    .orElse("Unknown User");
        } catch(NumberFormatException e) {
            System.err.println("AddUser - 잘못된 사용자 ID 형식: " + chatDTO.getSenderId());
        } catch (Exception e) {
            System.err.println("AddUser - 발신자 정보 조회 오류: " + e.getMessage());
        }

        chatDTO.setSenderName(senderName);
        chatDTO.setMessage(senderName + " 님이 입장하셨습니다.");
        chatDTO.setType(ChatDTO.MessageType.ENTER);
        chatDTO.setTimestamp(LocalDateTime.now().format(formatter));
        chatDTO.setProjectId(projectId);

        String destination = "/topic/chat/project/" + projectId;
        messagingTemplate.convertAndSend(destination, chatDTO);
    }

    @GetMapping("/api/chat/history/{projectId}")
    @ResponseBody
    public ResponseEntity<List<ChatDTO>> getChatHistory(
            @PathVariable("projectId") Integer projectId,
            @AuthenticationPrincipal UserDetails currentUserDetails
    ) {
        if (currentUserDetails == null) {
            System.err.println("채팅 기록 조회 실패: 인증되지 않은 사용자입니다. (currentUserDetails is null)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        UserEntity currentUser;
        try {
            String username = currentUserDetails.getUsername();
            // userService.findByUsername(String) 호출로 변경하고, Optional 처리
            currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        } catch (UsernameNotFoundException e) { // 구체적인 예외 처리
            System.err.println("채팅 기록 조회 실패: 사용자 정보를 가져오는 중 오류 발생 (사용자 없음). " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList()); // 사용자를 못찾으면 권한 없음 처리
        } catch (Exception e) { // 그 외 예외
            System.err.println("채팅 기록 조회 실패: 사용자 정보를 가져오는 중 알 수 없는 오류 발생. " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }

        try {
            List<ChatDTO> history = chatService.getChatHistory(projectId, currentUser);
            return ResponseEntity.ok(history);
        } catch (EntityNotFoundException e) {
            System.err.println("채팅 기록 조회 실패 (엔티티 없음): " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            System.err.println("채팅 기록 조회 실패 (권한 없음): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("채팅 기록 조회 중 서버 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
