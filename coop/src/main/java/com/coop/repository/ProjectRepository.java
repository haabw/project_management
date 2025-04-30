package com.coop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coop.entity.ProjectEntity;
import com.coop.entity.UserEntity;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer> {
	// 소유자(owner)로 프로젝트 리스트 조회
		List<ProjectEntity> findByOwner(UserEntity owner);
}
