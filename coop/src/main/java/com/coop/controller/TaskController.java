package com.coop.controller;

import com.coop.dto.TaskDTO;
import com.coop.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }
    // 새 작업 생성
    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        return taskService.saveTask(taskDTO);
    }
    // 기존 작업 수정
    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Integer id, @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(id, taskDTO);
    }
    // 작업 삭제
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
    }
}
