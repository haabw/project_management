package com.coop.service;

import com.coop.entity.ProjectEntity;
import com.coop.entity.TaskEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.ProjectRepository;
import com.coop.repository.TaskRepository;
import com.coop.repository.UserRepository;
import com.coop.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;

    // 모든 Task 데이터를 가져와서 DTO 리스트로 반환
    public List<TaskDTO> getAllTasks() {
        List<TaskEntity> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Task 저장
    public TaskDTO saveTask(TaskDTO dto) {
        TaskEntity task = convertToEntity(dto);
        TaskEntity saved = taskRepository.save(task);
        return convertToDTO(saved);
    }

    // Task 삭제
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    // Task 수정
    public TaskDTO updateTask(Integer id, TaskDTO dto) {
    	System.out.println(dto);
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        ProjectEntity project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setName(dto.getName());
        task.setStart(dto.getStart());
        task.setEnd(dto.getEnd());
        task.setDuration(dto.getDuration());
        System.out.println(dto.getProgress());
        task.setProgress(dto.getProgress());
        task.setStatus(dto.getStatus());
        task.setProject(project);
        task.setPriority(dto.getPriority());

        TaskEntity updated = taskRepository.save(task);
        return convertToDTO(updated);
    }

    // Entity -> DTO
    private TaskDTO convertToDTO(TaskEntity task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setStart(task.getStart());
        dto.setEnd(task.getEnd());
        dto.setDuration(task.getDuration());
        dto.setProgress(task.getProgress());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setProjectId(task.getProject() != null ? task.getProject().getProjectId() : null);
        if (task.getWorkers() != null && !task.getWorkers().isEmpty()) {
            dto.setWorkerId(task.getWorkers().stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList()));

            dto.setWorkerNames(task.getWorkers().stream()
                    .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                    .collect(Collectors.toList()));
        } else {
            dto.setWorkerId(List.of());
            dto.setWorkerNames(List.of());
        }
        return dto;
    }

    // DTO -> Entity
    private TaskEntity convertToEntity(TaskDTO dto) {
        TaskEntity task = new TaskEntity();
        task.setId(dto.getId());
        task.setName(dto.getName());
        task.setStart(dto.getStart());
        task.setEnd(dto.getEnd());
        task.setDuration(dto.getDuration());
        task.setProgress(dto.getProgress());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        if (dto.getWorkerId() != null) {
            List<UserEntity> users = dto.getWorkerId().stream()
                .map(userId -> userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId)))
                .collect(Collectors.toList());
            task.setWorkers(users);
        }
        
        if (dto.getProjectId() != null) {
            ProjectEntity project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            task.setProject(project);
        }

        return task;
    }
public List<TaskDTO> getTasksByProjectId(Integer projectId) {
    return taskRepository.findByProject_ProjectId(projectId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
	}
}