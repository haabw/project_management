package com.coop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coop.entity.ProjectEntity;
import com.coop.entity.ProjectMemberEntity.ProjectStatus;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer> {
	// JOIN FETCH p.members 해서 members 컬렉션까지 함께 로드
	@Query("SELECT p FROM ProjectEntity p LEFT JOIN FETCH p.members WHERE p.projectId = :id")
	Optional<ProjectEntity> findWithMembersById(@Param("id") int id);
	
	/** 로그인한 userId, status=APPROVED 인 프로젝트만 조회 */
	List<ProjectEntity>
    findAllByMembers_User_UsernameAndMembers_Status(
      String username,
      ProjectStatus status
    );
}
