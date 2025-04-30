package com.coop.dto;

import java.time.LocalDate;

public class TaskDTO {
	 private Integer id;           	  // 작업 ID
	    private String name;          // 작업 이름
	    private LocalDate start;      // 시작 날짜
	    private LocalDate end;        // 종료 날짜
	    private Integer duration;     // 기간
	    private Integer progress;     // 진행률
	    private String status;        // 상태 (진행중, 완료 등)
	    private Integer parentId;     // 상위 작업 ID
	    private Integer assigneeId;   // 담당자 ID
	// 기본 생성자
    public TaskDTO() {}

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

    public Integer getAssigneeId() {
        return assigneeId;
    }
    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }
}
