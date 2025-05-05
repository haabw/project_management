package com.coop.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC) //매개 변수 없는 기본 생성자를 퍼플릭으로 생성 
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto increment 기능하는 기본 키 
    @Column(name = "user_id", updatable = false) // id -> user_id 로 수정, updatable = false 은 업데이트 불가 설정 
    private int id;

    @Column(name = "username", nullable = false, unique = true) // nullable은 null 가능 여부, unique는 고유 값 
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist // 엔티티가 저장되기 직전에 호출되는 메서드, 즉 onCreate가 호출됨 
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }
    @PreUpdate // 엔티티가 업데이트되기 직전에 호출되는 메서드, 즉 onUpdate가 호출됨 
    protected void onUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }
    public void setProfileImage(String base64Image) {
		// TODO Auto-generated method stub
		
	}
}