package com.coop.entity;

import jakarta.persistence.*;
import lombok.Data; // Lombok @Data 사용 (Getter, Setter, ToString, EqualsAndHashCode, RequiredArgsConstructor 포함)
import lombok.NoArgsConstructor; // 기본 생성자 추가
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects") // 테이블 이름을 'projects'로 사용 (이전 SQL 스크립트와 일치)
@Data // Lombok 어노테이션 사용
@NoArgsConstructor // JPA는 기본 생성자가 필요
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id") // 컬럼 이름 명시
    private Long projectId; // ID 타입을 Long으로 사용 (ProjectRepository와 일치)

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "created_date", updatable = false) // 생성 시간 컬럼
    private LocalDateTime createdDate;

    // ChatEntity와의 양방향 관계 설정 (ChatEntity의 'project' 필드와 매핑됨)
    // 프로젝트 삭제 시 관련 채팅 메시지도 함께 삭제 (cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // fetch = FetchType.LAZY 추가 (성능 최적화)
    private List<ChatEntity> chatMessages = new ArrayList<>();

    // 엔티티가 저장되기 전에 실행될 메소드
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // Lombok @Data가 getter/setter를 생성해주므로 명시적인 getter/setter는 필요 없음
    // public Long getProjectId() { return projectId; }
    // public void setProjectId(Long projectId) { this.projectId = projectId; }
    // ... 등등
}
