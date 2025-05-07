package com.coop.service;

import com.coop.dto.ChatDTO;
import com.coop.entity.ChatEntity;
import com.coop.entity.ProjectEntity;
import com.coop.entity.ProjectMemberEntity; // ProjectMemberEntity 임포트
import com.coop.entity.UserEntity;
import com.coop.repository.ChatRepository;
import com.coop.repository.ProjectMemberRepository; // ProjectMemberRepository 임포트
import com.coop.repository.ProjectRepository;
import com.coop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository; // ProjectMemberRepository 주입

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 사용자가 특정 프로젝트의 승인된 멤버인지 확인합니다.
     *
     * @param user    확인할 사용자 엔티티
     * @param project 확인할 프로젝트 엔티티
     * @return 멤버이면 true, 아니면 false
     */
    private boolean isUserApprovedMemberOfProject(UserEntity user, ProjectEntity project) {
        return projectMemberRepository.existsByUserAndProjectAndStatus(
                user, project, ProjectMemberEntity.ProjectStatus.APPROVED);
    }

    @Transactional
    public ChatDTO saveMessage(ChatDTO chatDTO) {
        Long userId = Long.parseLong(chatDTO.getSenderId());
        Integer projectId = Integer.parseInt(chatDTO.getProjectId());

        UserEntity user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // 사용자가 해당 프로젝트의 승인된 멤버인지 확인
        if (!isUserApprovedMemberOfProject(user, project)) {
            // 멤버가 아니면 AccessDeniedException 발생 또는 로깅 후 null 반환 등의 처리
            throw new AccessDeniedException("사용자는 이 프로젝트의 멤버가 아닙니다. 메시지를 저장할 수 없습니다.");
        }

        ChatEntity chatEntity = ChatEntity.builder()
                .user(user)
                .project(project)
                .message(chatDTO.getMessage())
                .build();

        ChatEntity savedEntity = chatRepository.save(chatEntity);
        return convertToDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<ChatDTO> getChatHistory(Integer projectId, UserEntity currentUser) { // 현재 사용자 정보를 받도록 수정
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // 사용자가 해당 프로젝트의 승인된 멤버인지 확인
        if (!isUserApprovedMemberOfProject(currentUser, project)) {
            throw new AccessDeniedException("해당 프로젝트의 채팅 기록에 접근할 권한이 없습니다.");
        }

        List<ChatEntity> chatHistory = chatRepository.findByProjectOrderByTimestampAsc(project);

        return chatHistory.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ChatDTO convertToDTO(ChatEntity entity) {
        return ChatDTO.builder()
                .type(ChatDTO.MessageType.TALK)
                .projectId(String.valueOf(entity.getProject().getProjectId()))
                .senderId(String.valueOf(entity.getUser().getId()))
                .senderName(entity.getUser().getNickname())
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp().format(formatter))
                .build();
    }
}