package com.coop.repository;

// 필요한 클래스들을 임포트합니다.
import com.coop.entity.ProjectEntity; // ProjectEntity 엔티티 클래스
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA 리포지토리 인터페이스
import org.springframework.stereotype.Repository; // Spring의 Repository 빈으로 등록하기 위한 어노테이션

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    // JpaRepository를 상속받습니다.
    // 첫 번째 제네릭 파라미터: 관리할 엔티티 클래스 (ProjectEntity)
    // 두 번째 제네릭 파라미터: 해당 엔티티의 ID 필드 타입 (Long) - HEAD 브랜치의 타입 유지

}
