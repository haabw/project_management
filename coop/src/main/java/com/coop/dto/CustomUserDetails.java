// src/main/java/com/coop/dto/CustomUserDetails.java (수정)
package com.coop.dto;

import com.coop.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final int id; // 타입을 Long 에서 int 로 변경
    private final String username;
    private final String password;
    private final String role; // 역할 정보 (UserEntity에 없으므로 기본값 설정)

    // 생성자 수정: UserEntity 필드에 맞게 호출
    public CustomUserDetails(UserEntity userEntity) {
        this.id = userEntity.getId(); // getUserId() 대신 getId() 호출
        this.username = userEntity.getUsername();
        this.password = userEntity.getPassword();
        // UserEntity에 role 필드가 없으므로, 임시로 "USER" 역할을 부여합니다.
        // 추후 UserEntity에 role 필드를 추가하고 userEntity.getRole()로 변경할 수 있습니다.
        this.role = "USER";
    }

    // --- UserDetails 인터페이스 메서드 구현 ---

    // 사용자 ID 반환 Getter (반환 타입 int로 변경)
    public int getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역할(role) 정보를 GrantedAuthority 컬렉션으로 반환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // --- 계정 상태 관련 메서드 ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}