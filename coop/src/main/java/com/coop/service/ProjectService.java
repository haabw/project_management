package com.coop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coop.dto.ProjectDTO;
import com.coop.entity.ProjectEntity;
import com.coop.repository.ProjectRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.util.Optional; // Optional 임포트 추가!
import com.coop.entity.ProjectEntity; // ProjectEntity 임포트

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

	/* 전체 프로젝트 조회 */
	@Transactional(readOnly = true)
	public List<ProjectDTO> getAllProjects() {
		return projectRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
	}

	/* 프로젝트 수정 */
	public ProjectDTO updateProject(int id, ProjectDTO dto) {
		ProjectEntity existing = projectRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다. id=" + id));

		// Builder 로 새 엔티티 생성해서 덮어쓰기 (세터 없이 하기 위함)
		ProjectEntity updated = ProjectEntity.builder().projectId(existing.getProjectId())
				.projectName(dto.getProjectName()).createDate(existing.getCreateDate()).build();

		ProjectEntity saved = projectRepository.save(updated);
		return mapToDTO(saved);
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