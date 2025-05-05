package com.coop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coop.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {
	List<TaskEntity> findByProject_ProjectId(Integer projectId);
}