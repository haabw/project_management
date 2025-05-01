package com.coop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.coop.entity.UserEntity;
import com.coop.repository.UserRepository;

// 스프링 시큐리티의 사용자 인증을 위한 서비스 
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    // 의존성 주입
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // 사용자명으로 사용자 정보를 로드해 스프링 시큐리티에 userDetails 객체로 전달 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("해당 이름으로 사용자를 찾을 수 없습니다: " + username));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .build();
    }
}
