package com.coop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="todos")
@Getter
@Setter
public class TodoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "todo_id", updatable = false)
	private int id;

	@Column(name = "content", updatable = true) // 수정 가능하도록 updatable 변경
	private String content;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "completed")
	private boolean completed = false; // 완료 상태 (기본값: false)
}