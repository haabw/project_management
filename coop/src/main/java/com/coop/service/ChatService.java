package com.coop.service;

import com.coop.dto.ChatDTO;
import com.coop.entity.ChatEntity;
import com.coop.entity.ProjectEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.ChatRepository;
import com.coop.repository.ProjectRepository;
import com.coop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor; // Lombok 어노테이션 주석 처리
//import org.springframework.beans.factory.annotation.Autowired; // @Autowired 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 다시 활성화
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    // --- saveMessage 메소드 ---
    @Transactional
    public ChatDTO saveMessage(ChatDTO chatDTO) {
        UserEntity sender = userRepository.findById(chatDTO.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + chatDTO.getSenderId()));

        ProjectEntity project = projectRepository.findById(chatDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 프로젝트를 찾을 수 없습니다: " + chatDTO.getProjectId()));

        ChatEntity chatEntity = ChatEntity.builder()
                .user(sender)
                .project(project)
                .message(chatDTO.getMessage())
                .build();

        ChatEntity savedEntity = chatRepository.save(chatEntity);

        return ChatDTO.builder()
                .type(ChatDTO.MessageType.TALK)
                .projectId(savedEntity.getProject().getProjectId())
                .senderId(savedEntity.getUser().getId()) // .intValue() 없는 상태
                .senderName(savedEntity.getUser().getUsername())
                .message(savedEntity.getMessage())
                .timestamp(savedEntity.getTimestamp().format(formatter))
                .build();
    }

    // --- getChatHistory 메소드 ---
    @Transactional(readOnly = true)
    public List<ChatDTO> getChatHistory(Long projectId) {
        List<ChatEntity> history = chatRepository.findByProjectProjectIdOrderByTimestampAsc(projectId); // 이전 수정 사항 반영

        return history.stream()
                .map(entity -> ChatDTO.builder()
                        .type(ChatDTO.MessageType.TALK)
                        .projectId(entity.getProject().getProjectId())
                        .senderId(entity.getUser().getId()) // .intValue() 없는 상태
                        .senderName(entity.getUser().getUsername())
                        .message(entity.getMessage())
                        .timestamp(entity.getTimestamp().format(formatter))
                        .build())
                .collect(Collectors.toList());
    }
}