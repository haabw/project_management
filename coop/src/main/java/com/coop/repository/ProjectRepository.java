
package com.coop.repository;

import com.coop.entity.ProjectEntity; // ProjectEntity 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트 추가
import org.springframework.stereotype.Repository; // Repository 어노테이션 임포트 추가 (선택적이지만 권장)

// public class ProjectRepository { } // 변경 전

@Repository // 리포지토리 빈으로 인식하도록 어노테이션 추가
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> { // 클래스를 인터페이스로 변경하고 JpaRepository 상속
    // 필요한 경우 여기에 추가적인 쿼리 메소드를 정의할 수 있습니다.
    // 예: Optional<ProjectEntity> findByProjectName(String projectName);
}