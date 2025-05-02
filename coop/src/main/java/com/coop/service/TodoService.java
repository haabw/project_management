package com.coop.service;

import com.coop.entity.TodoEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

	private final TodoRepository todoRepository;

	public TodoService(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	public List<TodoEntity> getTodosByUser(UserEntity user) {
		return todoRepository.findByUser(user);
	}

	// Todo 추가 
	@Transactional
	public void addTodo(String content, UserEntity user) {
		TodoEntity todo = new TodoEntity();
		todo.setContent(content);
		todo.setUser(user);
		todo.setCompleted(false); // 기본값은 완료되지 않은 상태
		todoRepository.save(todo);
	}

	// Todo 삭제 
	@Transactional
	public void deleteTodo(int todoId, UserEntity user) {
		Optional<TodoEntity> todoOptional = todoRepository.findById(todoId);

		if (todoOptional.isPresent()) {
			TodoEntity todo = todoOptional.get();
			// 현재 로그인 사용자의 todo인지 확인
			if (todo.getUser().getId() == user.getId()) {
				todoRepository.deleteById(todoId);
			}
		}
	}

	// Todo 수정 - 비활성화 
//	@Transactional
//	public void updateTodoContent(int todoId, String content, UserEntity user) {
//		Optional<TodoEntity> todoOptional = todoRepository.findById(todoId);
//
//		if (todoOptional.isPresent()) {
//			TodoEntity todo = todoOptional.get();
//			// 현재 로그인 사용자의 todo인지 확인
//			if (todo.getUser().getId() == user.getId()) {
//				todo.setContent(content);
//				todoRepository.save(todo);
//			}
//		}
//	}

	// Todo 상태 
	@Transactional
	public boolean updateTodoStatus(int todoId, boolean completed, UserEntity user) {
		Optional<TodoEntity> todoOptional = todoRepository.findById(todoId);

		if (todoOptional.isPresent()) {
			TodoEntity todo = todoOptional.get();
			// 현재 로그인 사용자의 todo인지 확인
			if (todo.getUser().getId() == user.getId()) {
				todo.setCompleted(completed);
				todoRepository.save(todo);
				return true;
			}
		}
		return false;
	}
}