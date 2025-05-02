package com.coop.repository;

import com.coop.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    /**
     * 특정 프로젝트 ID에 해당하는 모든 채팅 메시지를 타임스탬프 오름차순(오래된 순)으로 조회합니다.
     *
     * @param projectId 조회할 프로젝트의 ID
     * @return 해당 프로젝트의 채팅 메시지 엔티티 목록 (시간순)
     */
    // List<ChatEntity> findByProjectIdOrderByTimestampAsc(Long projectId); // 변경 전
    List<ChatEntity> findByProjectProjectIdOrderByTimestampAsc(Long projectId); // 변경 후
}