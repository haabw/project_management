package com.coop.service;

import com.coop.entity.TaskEntity;
import com.coop.repository.TaskRepository;
import com.coop.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    // 모든 Task 데이터를 가져와서 DTO 리스트로 변환하여 반환
    public List<TaskDTO> getAllTasks() {
        List<TaskEntity> tasks = taskRepository.findAll(); // DB에서 모든 TaskEntity 조회
        return tasks.stream()
                .map(this::convertToDTO) // Entity를 DTO로 변환
                .collect(Collectors.toList());
    }
    // Task 데이터를 저장하고 저장된 결과를 DTO로 반환
    public TaskDTO saveTask(TaskDTO dto) {
        TaskEntity task = convertToEntity(dto); // DTO를 Entity로 변환
        TaskEntity saved = taskRepository.save(task); // DB에 저장
        return convertToDTO(saved); // 저장된 Entity를 DTO로 변환하여 반환
    }
    // ID를 기반으로 Task를 삭제
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id); // 해당 ID의 Task를 DB에서 삭제
    }
    // 특정 ID의 Task를 갱신하고 결과를 DTO로 반환
    public TaskDTO updateTask(Integer id, TaskDTO dto) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found")); // ID로 조회, 없으면 예외
        // DTO로부터 값들을 업데이트
        task.setName(dto.getName());
        task.setStart(dto.getStart());
        task.setEnd(dto.getEnd());
        task.setDuration(dto.getDuration());
        task.setProgress(dto.getProgress());
        task.setStatus(dto.getStatus());
        task.setParentId(dto.getParentId());
        task.setAssignedId(dto.getAssigneeId());

        TaskEntity updated = taskRepository.save(task); // 갱신된 Task 저장
        return convertToDTO(updated); // DTO로 변환하여 반환
    }
	// Entity -> DTO 변환 메서드
    private TaskDTO convertToDTO(TaskEntity task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setStart(task.getStart());
        dto.setEnd(task.getEnd());
        dto.setDuration(task.getDuration());
        dto.setProgress(task.getProgress());
        dto.setStatus(task.getStatus());
        dto.setParentId(task.getParentId());
        dto.setAssigneeId(task.getAssignedId());
        return dto;
    }
    // DTO -> Entity 변환 메서드
    private TaskEntity convertToEntity(TaskDTO dto) {
        TaskEntity task = new TaskEntity();
        task.setId(dto.getId());
        task.setName(dto.getName());
        task.setStart(dto.getStart());
        task.setEnd(dto.getEnd());
        task.setDuration(dto.getDuration());
        task.setProgress(dto.getProgress());
        task.setStatus(dto.getStatus());
        task.setParentId(dto.getParentId());
        task.setAssignedId(dto.getAssigneeId());
        return task;
    }
}

