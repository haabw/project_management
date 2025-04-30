package com.coop.dto;

public class TaskDependencyDTO {
	private Integer id;           // 의존 관계 ID
    private Integer fromTaskId;   // 선행 작업 ID (출발점)
    private Integer toTaskId;     // 후속 작업 ID (도착점)
    private String type;          // 의존 타입
    // 기본 생성자
    public TaskDependencyDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromTaskId() {
        return fromTaskId;
    }

    public void setFromTaskId(Integer fromTaskId) {
        this.fromTaskId = fromTaskId;
    }

    public Integer getToTaskId() {
        return toTaskId;
    }

    public void setToTaskId(Integer toTaskId) {
        this.toTaskId = toTaskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
