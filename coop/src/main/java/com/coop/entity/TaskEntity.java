package com.coop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;           // 작업 이름
    private LocalDate start;       // 작업 시작일
    private LocalDate end;         // 작업 종료일
    private Integer duration;      // 작업 기간 (일수 기준)
    private Integer progress;      // 작업 진행률 (0~100)
    private String priority;       // 우선 순위
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_workers",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> workers = new ArrayList<>();

    private String status;         // 작업 상태 (예: 진행중, 완료 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;
}
