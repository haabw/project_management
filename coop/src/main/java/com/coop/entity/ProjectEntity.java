package com.coop.entity;


import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.*; // 어노테이션을 쓰위 위함
import lombok.Getter;
import java.time.LocalDateTime; // 시간

//프로젝트 데이터 

@NoArgsConstructor(access = AccessLevel.PROTECTED) // 롬북 어노테이션 필수
@Getter // 롬북 어노테이션 필드값 얻기 위해
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
}
