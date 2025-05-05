package com.coop.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // JPA 엔티티는 기본 생성자가 필요합니다.
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_message") // 데이터베이스 테이블 이름 지정
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    // User 엔티티와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY) // 성능을 위해 지연 로딩 사용
    @JoinColumn(name = "user_id", nullable = false) // DB의 user_id 컬럼과 매핑, null 불가
    private UserEntity user; // 발신자 정보

    // Project 엔티티와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false) // DB의 project_id 컬럼과 매핑, null 불가
    private ProjectEntity project; // 메시지가 속한 프로젝트 정보

    @Column(name = "message", columnDefinition = "TEXT", nullable = false) // 메시지 내용, TEXT 타입
    private String message;

    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간 자동 저장
    @Column(name = "timestamp", nullable = false, updatable = false) // DB의 timestamp 타입과 매핑
    private LocalDateTime timestamp;
}