package com.coop.dto;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class ProjectDTO {
	private Integer projectId;
	private String projectName;
	private int ownerId;
	private LocalDateTime createDate;
}
