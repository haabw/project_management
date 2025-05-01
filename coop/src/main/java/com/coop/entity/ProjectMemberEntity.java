package com.coop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="project_members")
@Getter
@Setter
public class ProjectMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne // 다대일 관계, 유저 한명이 여러 프로젝트의 멤버가 될 수 있다. 
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="project_id", nullable=false) // 외래키로 지정 
    private ProjectEntity project;

    @Enumerated(EnumType.STRING) // enum 타입이 데이터베이스에 저장됨 
    @Column(nullable=false)
    private ProjectRole role;

    // ✨ 추가해야 할 부분: 초대 수락/거절 상태 관리
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ProjectStatus status;

    // admin = (초대, 추방, 편집), editor = 읽고 쓰기 가능, user = 읽기 전용 
    public enum ProjectRole {
        ADMIN, EDITOR, USER
    }

    // ✨ 추가: 초대 상태 (PENDING = 대기중, APPROVED = 수락 완료)
    public enum ProjectStatus {
        PENDING, APPROVED
    }
}
