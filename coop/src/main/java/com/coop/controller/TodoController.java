package com.coop.controller;

import com.coop.service.TodoService;
import com.coop.service.UserService;
import com.coop.entity.TodoEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
public class TodoController {

	private final TodoService todoService;
	private final UserService userService;
	private final UserRepository userRepository;

	public TodoController(TodoService todoService, UserService userService, UserRepository userRepository) {
		this.todoService = todoService;
		this.userService = userService;
		this.userRepository = userRepository;
	}

	// 투두 리스트 뷰, 사용자를 조회해서 현재 사용자에 대한 투두를 페이지에 뿌려줌 
	@GetMapping("/todo")
	public String todoPage(Model model) {
		// 현재 로그인 사용자 조회
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = auth.getName();

		// UserService를 통해 현재 사용자 조회
		Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
		if (optionalUser.isEmpty())
			return "redirect:/login"; // 로그인 정보 없을시 로그인 페이지로
		UserEntity currentUser = optionalUser.get();

		// 사용자 입력 Todo 조회
		List<TodoEntity> customTodos = todoService.getTodosByUser(currentUser);
		model.addAttribute("customTodos", customTodos);

		// 간트차트 Todo 조회 (여기서는 예시로 빈 리스트 전달, 실제 구현은 필요에 따라 작성)
		// List<TodoEntity> ganttTodos = ganttService.getGanttTodosByUser(currentUser);
		// model.addAttribute("ganttTodos", ganttTodos);

		return "todo";
	}

	// Todo 추가 
	@PostMapping("/todo")
	public String addTodo(@RequestParam("newTodo") String newTodo) {
		// 현재 로그인 사용자 조회
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = auth.getName();

		// UserService를 통해 현재 사용자 조회
		Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
		if (optionalUser.isEmpty())
			return "redirect:/login"; // 로그인 정보 없을시 로그인 페이지로
		UserEntity currentUser = optionalUser.get();

		// 새로운 todo 추가
		todoService.addTodo(newTodo, currentUser);
		return "redirect:/todo";
	}

	// Todo 삭제
	@PostMapping("/todo/delete")
	public String deleteTodo(@RequestParam("todoId") int todoId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = auth.getName();

		Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
		if (optionalUser.isEmpty())
			return "redirect:/login";
		UserEntity currentUser = optionalUser.get();

		todoService.deleteTodo(todoId, currentUser);
		return "redirect:/todo";
	}

	// Todo 내용 수정 , 필요 없을 것 같아서 비활성화 
//	@PostMapping("/todo/update")
//	public String updateTodo(@RequestParam("todoId") int todoId, @RequestParam("content") String content) {
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		String currentUsername = auth.getName();
//
//		Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
//		if (optionalUser.isEmpty())
//			return "redirect:/login";
//		UserEntity currentUser = optionalUser.get();
//
//		todoService.updateTodoContent(todoId, content, currentUser);
//		return "redirect:/todo";
//	}

	// Todo 완료 상태 변경 (AJAX 요청)
	@PostMapping("/todo/status")
	@ResponseBody
	public String updateTodoStatus(@RequestParam("todoId") int todoId, @RequestParam("completed") boolean completed) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String currentUsername = auth.getName();
	    Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
	    if (optionalUser.isEmpty()) return "error";
	    UserEntity currentUser = optionalUser.get();
	    boolean success = todoService.updateTodoStatus(todoId, completed, currentUser);
	    return success ? "success" : "error";
	}
}