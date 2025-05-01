package com.coop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="project_members")
@Data
public class ProjectMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="project_id", nullable=false)
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ProjectRole role;

    //초대 수락/거절 상태 관리
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ProjectStatus status;

    public enum ProjectRole {
        ADMIN, EDITOR, USER
    }

    // 초대 상태 (PENDING = 대기중, APPROVED = 수락 완료)
    public enum ProjectStatus {
        PENDING, APPROVED
    }
}
