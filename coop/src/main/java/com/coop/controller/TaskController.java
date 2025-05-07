package com.coop.controller;

import com.coop.dto.TaskDTO;
import com.coop.entity.ProjectEntity;
import com.coop.repository.ProjectRepository;
import com.coop.repository.UserRepository;
import com.coop.service.TaskService;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    
    
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    // 전체 작업 목록 조회
    @GetMapping
    public List<TaskDTO> getTasks(@RequestParam(value = "projectId", required = false) Integer projectId) {
        if (projectId != null) {
            return taskService.getTasksByProjectId(projectId);
        } else {
            return taskService.getAllTasks();
        }
    }
    // 새 작업 생성
    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        return taskService.saveTask(taskDTO);
    }
    // 기존 작업 수정
    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Integer id, @RequestBody TaskDTO taskDTO) {
    	System.out.println(taskDTO);
        return taskService.updateTask(id, taskDTO);
    }
    // 작업 삭제
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
    }
    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/users")
    public List<UserDTO> getUsersByProject(@RequestParam("projectId") Integer projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        return project.getMembers().stream()
            .map(pm -> pm.getUser()) // 중간 테이블 통해 유저 꺼냄
            .map(user -> {
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                return dto;
            }).toList();
    }


    @Getter @Setter
    private static class UserDTO {
        private int id;
        private String name;
    }

}
