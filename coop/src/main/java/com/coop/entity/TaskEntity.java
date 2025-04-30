package com.coop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskEntity {

    @Id
    private Integer id;

    private String name;           // 작업 이름
    private LocalDate start;       // 작업 시작일
    private LocalDate end;         // 작업 종료일
    private Integer duration;      // 작업 기간 (일수 기준)
    private Integer progress;      // 작업 진행률 (0~100)
    private String status;         // 작업 상태 (예: 진행중, 완료 등)
    private Integer parentId;      // 상위 작업 ID (없으면 null)
    private Integer assignedId;    // 담당자 ID
}
