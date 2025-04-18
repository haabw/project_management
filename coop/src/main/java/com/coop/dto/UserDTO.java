package com.coop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
//로그인 요청 데이터 

@Data
@NoArgsConstructor
public class UserDTO {
	private String id;
	private String username;
	private String email;
	private String nickname;
	private String role;
	private LocalDateTime createDate;
	private LocalDateTime modifiedDate;
}
