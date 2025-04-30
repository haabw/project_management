
package com.coop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
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

    // 기본 생성자
    public TaskEntity() {}
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(Integer assignedId) {
        this.assignedId = assignedId;
    }
}
