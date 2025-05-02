package com.coop.service;

import com.coop.dto.ProjectDTO;
import com.coop.entity.ProjectEntity;
import com.coop.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    /* 프로젝트 추가 */
    // ... (기존 createProject 코드 유지) ...
    public ProjectDTO createProject(ProjectDTO dto) {
        ProjectEntity entity = ProjectEntity.builder()
                .projectName(dto.getProjectName())
                // .createDate(LocalDateTime.now()) // createDate는 @PrePersist로 자동 설정되므로 제거 가능
                .build();

        ProjectEntity saved = projectRepository.save(entity);
        return mapToDTO(saved);
    }


    /* 전체 프로젝트 조회 */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null) // 혹시 모를 null 방지
                .collect(Collectors.toList());
    }

    /* 프로젝트 수정 */
    // ... (기존 updateProject 코드 유지) ...
    // update 시 projectId 타입 Long으로 수정 필요
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) { // 파라미터 타입을 Long으로 변경
        ProjectEntity existing = projectRepository.findById(id) // findById의 파라미터도 Long
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다. id=" + id));

        // 프로젝트 이름만 업데이트하는 것으로 변경 (다른 필드는 유지)
        existing.setProjectName(dto.getProjectName());
        // createdDate는 보통 수정하지 않음

        ProjectEntity saved = projectRepository.save(existing);
        return mapToDTO(saved);
    }


    /* 프로젝트 삭제 */
    // ... (기존 deleteProject 코드 유지) ...
    // delete 시 projectId 타입 Long으로 수정 필요
    @Transactional
    public void deleteProject(Long id) { // 파라미터 타입을 Long으로 변경
        projectRepository.deleteById(id); // deleteById의 파라미터도 Long
    }


    /* 엔티티 DTO 변환 수정 */
    private ProjectDTO mapToDTO(ProjectEntity e) {
        if (e == null) {
            return null;
        }
        // ProjectDTO의 projectId 타입이 Long으로 변경되었으므로, 형변환이나 null 체크 후 intValue() 호출 불필요
        return ProjectDTO.builder()
                .projectId(e.getProjectId()) // 바로 Long 타입 사용
                .projectName(e.getProjectName())
                .createDate(e.getCreatedDate()) // Lombok이 생성한 getCreatedDate() 사용
                .build();
    }
}