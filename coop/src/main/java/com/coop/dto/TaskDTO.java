package com.coop.dto;

import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {

    private Integer id;           // 작업 ID
    private String name;          // 작업 이름
    private LocalDate start;      // 시작 날짜
    private LocalDate end;        // 종료 날짜
    private Integer duration;     // 기간
    private Integer progress;     // 진행률
    private String status;        // 상태 (진행중, 완료 등)
    private Integer projectId;
    private List<Integer> workerId;   // 작업자 ID
    private List<String> workerNames;
    private String priority;  			// 우선 순위
}