package com.coop.repository;

import com.coop.entity.ChatEntity;
import com.coop.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    /**
     * 특정 프로젝트의 모든 채팅 메시지를 시간 순서대로 조회합니다.
     * User 엔티티를 함께 로딩하여 N+1 문제를 방지합니다. (JOIN FETCH)
     *
     * @param project 조회할 프로젝트 엔티티
     * @return 시간 순으로 정렬된 채팅 엔티티 리스트
     */
    @Query("SELECT ce FROM ChatEntity ce JOIN FETCH ce.user WHERE ce.project = :project ORDER BY ce.timestamp ASC")
    List<ChatEntity> findByProjectOrderByTimestampAsc(@Param("project") ProjectEntity project);

    /**
     * (대체 가능) 프로젝트 ID를 이용하여 채팅 메시지를 시간 순으로 조회합니다.
     * JOIN FETCH가 없으므로 각 메시지마다 사용자 정보를 다시 조회할 수 있습니다 (N+1 문제 발생 가능성).
     *
     * @param projectId 조회할 프로젝트의 ID
     * @return 시간 순으로 정렬된 채팅 엔티티 리스트
     */
   //현재 오류 확인을 위해 주석처리 20250505 08 34
    // List<ChatEntity> findByProjectIdOrderByTimestampAsc(Integer projectId);
}