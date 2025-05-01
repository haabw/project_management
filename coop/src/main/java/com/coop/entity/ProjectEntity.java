package com.coop.entity;

import java.time.LocalDateTime;

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

	public ProjectEntity(int projectId, String projectName, LocalDateTime createDate) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.createDate = createDate;
	}

}