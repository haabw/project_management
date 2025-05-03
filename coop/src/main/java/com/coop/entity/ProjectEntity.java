package com.coop.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Entity
@Table(name = "project")
public class ProjectEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "project_id")
	private int projectId;

	@Column(name = "project_name", nullable = false)
	private String projectName;

	@Column(name = "create_date")
	private LocalDateTime createDate;

	@Builder.Default
	@OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ProjectMemberEntity> members = new ArrayList<>();

	public ProjectEntity(int projectId, String projectName, LocalDateTime createDate,
			List<ProjectMemberEntity> members) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.createDate = createDate;
		this.members = members;
	}

}