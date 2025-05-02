package com.coop.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDTO {
	private int projectId;
	private String projectName;
	private LocalDateTime createDate;

	public ProjectDTO(int projectId, String projectName, LocalDateTime createDate) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.createDate = createDate;
	}

}