package com.coop.repository;

import com.coop.entity.ProjectEntity;
import com.coop.entity.ProjectMemberEntity;
import com.coop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Optional 임포트 추가

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Integer> {

    /**
     * 사용자와 프로젝트, 그리고 멤버 상태를 기준으로 프로젝트 멤버 정보를 조회합니다.
     * @param user 사용자 엔티티
     * @param project 프로젝트 엔티티
     * @param status 조회하고자 하는 멤버 상태 (예: ProjectMemberEntity.ProjectStatus.APPROVED)
     * @return ProjectMemberEntity 객체를 담은 Optional
     */
    Optional<ProjectMemberEntity> findByUserAndProjectAndStatus(UserEntity user, ProjectEntity project, ProjectMemberEntity.ProjectStatus status);

    // 프로젝트 ID와 사용자 ID로 멤버인지 확인하는 메소드 (승인된 멤버만)
    boolean existsByUserAndProjectAndStatus(UserEntity user, ProjectEntity project, ProjectMemberEntity.ProjectStatus status);
}