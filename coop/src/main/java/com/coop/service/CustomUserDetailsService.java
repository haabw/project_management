package com.coop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.coop.entity.UserEntity;
import com.coop.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("해당 이름으로 사용자를 찾을 수 없습니다: " + username));

		// 닉네임을 포함한 커스텀 User 객체 반환
		return new CustomUser(user);
	}

	// 닉네임을 포함하기 위한 커스텀 User 클래스
	public static class CustomUser extends org.springframework.security.core.userdetails.User {
		private final String nickname;

		public CustomUser(UserEntity user) {
			// 기본 권한 "ROLE_USER" 설정
			super(user.getUsername(), user.getPassword(),
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
			this.nickname = user.getNickname();
		}

		public String getNickname() {
			return nickname;
		}
	}
}