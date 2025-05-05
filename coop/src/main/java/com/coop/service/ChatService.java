package com.coop.service;

import com.coop.dto.ChatDTO;
import com.coop.entity.ChatEntity;
import com.coop.entity.ProjectEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.ChatRepository;
import com.coop.repository.ProjectRepository;
import com.coop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입 (Lombok)
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;         // 사용자 정보 조회를 위해 주입
    private final ProjectRepository projectRepository;   // 프로젝트 정보 조회를 위해 주입

    // 클라이언트에 표시될 날짜/시간 형식
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 채팅 메시지를 데이터베이스에 저장합니다.
     *
     * @param chatDTO 저장할 채팅 메시지 데이터 (클라이언트에서 받은 정보)
     * @return 저장된 채팅 메시지 데이터 (DB 정보 포함)
     */
    @Transactional // 데이터 변경이 있으므로 트랜잭션 처리
    public ChatDTO saveMessage(ChatDTO chatDTO) {
        // DTO에서 ID 추출 (타입 변환 주의)
        Long userId = Long.parseLong(chatDTO.getSenderId());
        Integer projectId = Integer.parseInt(chatDTO.getProjectId());

        // ID를 사용하여 UserEntity와 ProjectEntity 조회 (DB 접근)
        // orElseThrow: 해당 ID의 엔티티가 없으면 예외 발생
        UserEntity user = userRepository.findById(userId.intValue()) // Long -> Integer 변환 필요 (UserEntity의 ID 타입이 Integer이므로)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // ChatDTO를 ChatEntity로 변환
        ChatEntity chatEntity = ChatEntity.builder()
                .user(user)         // 조회한 UserEntity 설정
                .project(project)   // 조회한 ProjectEntity 설정
                .message(chatDTO.getMessage())
                // timestamp는 @CreationTimestamp에 의해 자동 생성되므로 설정 불필요
                .build();

        // Repository를 통해 ChatEntity를 DB에 저장
        ChatEntity savedEntity = chatRepository.save(chatEntity);

        // 저장된 Entity를 다시 ChatDTO로 변환하여 반환 (ID, timestamp 등 포함)
        return convertToDTO(savedEntity);
    }

    /**
     * 특정 프로젝트의 모든 채팅 기록을 조회합니다.
     *
     * @param projectId 조회할 프로젝트의 ID
     * @return 해당 프로젝트의 채팅 기록 리스트 (DTO)
     */
    @Transactional(readOnly = true) // 데이터 조회만 하므로 readOnly 설정 (성능 향상)
    public List<ChatDTO> getChatHistory(Integer projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

        // 여기에서 @Query가 있는 메소드를 사용하는지 확인!
        List<ChatEntity> chatHistory = chatRepository.findByProjectOrderByTimestampAsc(project);

        return chatHistory.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * ChatEntity 객체를 ChatDTO 객체로 변환하는 내부 헬퍼 메서드.
     *
     * @param entity 변환할 ChatEntity 객체
     * @return 변환된 ChatDTO 객체
     */
    private ChatDTO convertToDTO(ChatEntity entity) {
        return ChatDTO.builder()
                .type(ChatDTO.MessageType.TALK) // DB 조회 결과는 기본 TALK 타입
                .projectId(String.valueOf(entity.getProject().getProjectId())) // Integer -> String
                .senderId(String.valueOf(entity.getUser().getId()))           // Integer -> String
                .senderName(entity.getUser().getNickname()) // UserEntity에서 닉네임 가져오기
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp().format(formatter)) // LocalDateTime을 지정된 형식의 문자열로 변환
                .build();
    }
}