package com.coop.repository;

import com.coop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

//사용자 쿼리 
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username); // 아이디 중복 확인 
    Optional<UserEntity> findByNickname(String nickname); // 닉네임 중복 확인 
    Optional<UserEntity> findByEmail(String email); // 이메일 중복 확인 
}