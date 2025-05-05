package com.coop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskDependencyDTO {

    private Integer id;           // 의존 관계 ID
    private Integer fromTaskId;   // 선행 작업 ID (출발점)
    private Integer toTaskId;     // 후속 작업 ID (도착점)
    private String type;          // 의존 타입
}