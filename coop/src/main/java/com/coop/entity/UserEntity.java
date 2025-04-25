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
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", updatable = false)
	private int id;
	
	@Column(name="username")
	private String username;
	
	@Column(name="password")
	private String password;
	
	@Column(name="email")
	private String email;
	
	@Column(name="nickname")
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
