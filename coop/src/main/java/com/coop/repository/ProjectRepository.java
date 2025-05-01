    package com.coop.repository;

    import com.coop.entity.ProjectEntity; // ProjectEntity 임포트
    import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트
    import org.springframework.stereotype.Repository; // Repository 어노테이션 임포트

    import java.util.List; // 필요시 주석 해제
    import java.util.Optional; // 필요시 주석 해제

    /**
    * ProjectEntity에 대한 데이터 접근을 처리하는 JPA 리포지토리 인터페이스입니다.
    */
    @Repository // 이 인터페이스가 Spring Data JPA 리포지토리 빈임을 나타냅니다.
    public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
        // JpaRepository<엔티티 클래스, ID 타입>

        // 필요한 경우 여기에 추가적인 쿼리 메소드를 정의할 수 있습니다.
        // Spring Data JPA는 메소드 이름을 분석하여 자동으로 쿼리를 생성합니다.
        // 예시: 프로젝트 이름으로 프로젝트 찾기
        // Optional<ProjectEntity> findByProjectName(String projectName);

        // 예시: 특정 사용자가 참여한 프로젝트 목록 찾기 (ProjectMemberEntity 등이 필요)
        // List<ProjectEntity> findByMembersUserId(Long userId);
    }
