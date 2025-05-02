package com.coop.service;

import com.coop.dto.CustomUserDetails; // CustomUserDetails 임포트
import com.coop.entity.UserEntity;
import com.coop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional; // Optional 임포트

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자 이름(username)으로 UserEntity 조회
        Optional<UserEntity> userEntityOptional = userRepository.findByUsername(username);

        // 사용자가 존재하지 않으면 예외 발생
        UserEntity userEntity = userEntityOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username));

        // UserEntity 정보를 사용하여 CustomUserDetails 객체 생성 및 반환
        return new CustomUserDetails(userEntity);

        /* 이전 코드 (주석 처리)
        return userRepository.findByUsername(username)
                .map(user -> User.builder() // 기본 User 객체 대신 CustomUserDetails 사용
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole()) // 기본 User.builder()에는 roles() 사용
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        */
    }
}