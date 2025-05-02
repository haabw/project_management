package com.coop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coop.entity.ProjectEntity;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer> {
	// JOIN FETCH p.members 해서 members 컬렉션까지 함께 로드
	@Query("SELECT p FROM ProjectEntity p LEFT JOIN FETCH p.members WHERE p.projectId = :id")
	Optional<ProjectEntity> findWithMembersById(@Param("id") int id);
}
