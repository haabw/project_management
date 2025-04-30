package com.coop.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.coop.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {}