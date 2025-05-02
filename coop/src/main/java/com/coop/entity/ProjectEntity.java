package com.coop.entity;

// 필요한 import 문들을 합칩니다.
import jakarta.persistence.*;
import lombok.Data; // Lombok @Data 사용 (Getter, Setter, ToString, EqualsAndHashCode, RequiredArgsConstructor 포함)
import lombok.NoArgsConstructor; // Lombok: 기본 생성자 추가 (JPA 요구사항)
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 프로젝트 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity // 이 클래스가 JPA 엔티티임을 나타냅니다.
@Table(name = "projects") // 데이터베이스의 'projects' 테이블과 매핑됩니다.
@Data // Lombok: Getter, Setter, toString(), equals(), hashCode() 메소드를 자동으로 생성합니다.
@NoArgsConstructor // Lombok: 파라미터 없는 기본 생성자를 자동으로 생성합니다. JPA는 기본 생성자를 필요로 합니다.
public class ProjectEntity {

    /**
     * 프로젝트의 고유 식별자 (Primary Key)
     */
    @Id // 기본 키 필드임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 데이터베이스의 IDENTITY 컬럼을 사용하여 기본 키 값을 자동으로 생성합니다.
    @Column(name = "project_id") // 데이터베이스 테이블의 'project_id' 컬럼과 매핑됩니다.
    private Long projectId; // ID는 일반적으로 Long 타입을 사용합니다.

    /**
     * 프로젝트의 이름
     */
    @Column(name = "project_name", nullable = false) // 'project_name' 컬럼과 매핑되며, null 값을 허용하지 않습니다.
    private String projectName;

    /**
     * 프로젝트 생성 날짜 및 시간
     * 이 값은 엔티티가 처음 저장될 때 설정되며, 이후에는 업데이트되지 않습니다.
     */
    @Column(name = "created_date", updatable = false) // 'created_date' 컬럼과 매핑되며, 업데이트 작업 시 이 컬럼은 제외됩니다.
    private LocalDateTime createdDate;

    /**
     * 이 프로젝트에 속한 채팅 메시지 목록 (ChatEntity와의 관계)
     * 프로젝트가 삭제되면 관련된 채팅 메시지도 함께 삭제됩니다 (cascade = CascadeType.ALL).
     * 컬렉션에서 ChatEntity가 제거되면 데이터베이스에서도 해당 ChatEntity가 삭제됩니다 (orphanRemoval = true).
     * 관련된 채팅 메시지는 필요할 때 로드됩니다 (fetch = FetchType.LAZY).
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatEntity> chatMessages = new ArrayList<>(); // NullPointerException 방지를 위해 빈 리스트로 초기화합니다.

    /**
     * 엔티티가 데이터베이스에 저장되기 직전에 호출되는 메소드입니다.
     * 생성 날짜를 현재 시간으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // Lombok의 @Data 어노테이션이 필요한 getter, setter 메소드들을
    // 컴파일 시점에 자동으로 생성해주므로, 여기에 명시적으로 작성할 필요가 없습니다.
    // (예: getProjectId(), setProjectId(), getProjectName(), setProjectName(), getCreatedDate(), getChatMessages(), setChatMessages() 등)
}
