package com.coop.dto;

import com.coop.entity.TodoEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoDTO {

	private int id;
	private String content;
	private boolean completed;

	// 생성자: TodoEntity를 TodoDTO로 변환
	public TodoDTO(TodoEntity todoEntity) {
		this.id = todoEntity.getId();
		this.content = todoEntity.getContent();
		this.completed = todoEntity.isCompleted();
	}
}