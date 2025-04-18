package com.coop.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
//사용자 데이터 

@Entity
@Table(name="users")
@Data
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String username;
	private String password;
	private String email;
	private String nickname;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime modifiedDate;
	
	public enum Role {
		ADMIN, USER
	}
	
}
