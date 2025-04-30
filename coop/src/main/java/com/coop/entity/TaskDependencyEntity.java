package com.coop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskDependencyEntity {

    @Id
    private Integer id;

    private Integer fromTaskId;  // 의존 관계 시작 작업 ID
    private Integer toTaskId;    // 의존 관계 대상 작업 ID
    private String type;         // 의존 유형 (예: FS, SS, FF, SF 등)
}
