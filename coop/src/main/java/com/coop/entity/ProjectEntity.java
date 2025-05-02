package com.coop.entity;

import jakarta.persistence.*;
import lombok.Data; // @Data 어노테이션 확인
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data // <- 이 어노테이션이 있는지 확인!
@NoArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId; // 타입 Long 확인

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // 필드명 createdDate 확인

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatEntity> chatMessages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}