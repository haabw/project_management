package com.coop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Optional 임포트 추가!
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coop.dto.ProjectDTO;
import com.coop.entity.ProjectEntity;
import com.coop.entity.ProjectMemberEntity.ProjectStatus;
import com.coop.repository.ProjectRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepository;
	/* 프로젝트 추가 */
	public ProjectDTO createProject(ProjectDTO dto) {
		ProjectEntity entity = ProjectEntity.builder().projectName(dto.getProjectName()).createDate(LocalDateTime.now())
				.build();

		ProjectEntity saved = projectRepository.save(entity);
		return mapToDTO(saved);
	}

	/** 로그인한 userId 가 APPROVED 상태인 프로젝트만 DTO 로 반환 */
	@Transactional(readOnly = true)
	  public List<ProjectDTO> getMyApprovedProjects(String username) {
	    return projectRepository
	      .findAllByMembers_User_UsernameAndMembers_Status(
	        username, ProjectStatus.APPROVED
	      )
	      .stream()
	      .map(this::mapToDTO)
	      .toList();
	  }

	/** 프로젝트 이름 수정 */
    @Transactional
    public ProjectDTO updateProjectName(int projectId, String newName) {
        // 1) 기존 엔티티 조회
        ProjectEntity entity = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("프로젝트가 없습니다. id=" + projectId));

        // 2) 이름만 변경 (JPA 변경 감지)
        entity.setProjectName(newName);

        // 3) save 호출도 가능
        ProjectEntity saved = projectRepository.save(entity);

        // 4) DTO 반환
        return ProjectDTO.builder()
                .projectId(saved.getProjectId())
                .projectName(saved.getProjectName())
                .createDate(saved.getCreateDate())
                .build();
    }

	/* 프로젝트 삭제 */
	@Transactional
    public void deleteProject(int id) {
        // 1) 자식까지 한 번에 로딩
        ProjectEntity project = projectRepository.findWithMembersById(id)
            .orElseThrow(() -> new EntityNotFoundException("프로젝트 없음: " + id));

        // 2) 컬렉션 비우기 → orphanRemoval 작동해서 project_members 삭제 SQL 생성
        project.getMembers().clear();

        // 3) 부모 삭제 → cascade REMOVE(혹은 orphanRemoval) 적용
        projectRepository.delete(project);
    }

	/* 엔티티 DTO 변환 */
	private ProjectDTO mapToDTO(ProjectEntity e) {
		return ProjectDTO.builder().projectId(e.getProjectId()).projectName(e.getProjectName())
				.createDate(e.getCreateDate()).build();
	}
    /* 프로젝트 ID로 프로젝트 엔티티를 조회합니다.
    * @param id 조회할 프로젝트 ID
    * @return Optional<ProjectEntity> (프로젝트가 없으면 비어있는 Optional)
    */
   @Transactional(readOnly = true) // 데이터 조회만 하므로 readOnly 설정
   public Optional<ProjectEntity> findById(Integer id) {
       return projectRepository.findById(id);
   }

}