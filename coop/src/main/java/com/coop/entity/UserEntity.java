    package com.coop.entity;

    import lombok.AccessLevel;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import jakarta.persistence.*; // jakarta.persistence.* 로 임포트 확인

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "users") // 테이블 이름 확인
    @Data
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    public class UserEntity {

        @Id // 기본 키임을 명시
        @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 설정
        @Column(name = "id", updatable = false) // DB 컬럼명을 'id'로 명시적 지정
        private int id;

        @Column(name = "username", nullable = false, unique = true)
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

        // Lombok @Data가 getter/setter를 생성하므로 수동 getter는 필수는 아님
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getEmail() { return email; }
        public String getNickname() { return nickname; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getModifiedDate() { return modifiedDate; }


        @PrePersist
        protected void onCreate() {
            this.createdDate = LocalDateTime.now();
            this.modifiedDate = LocalDateTime.now();
        }
        @PreUpdate
        protected void onUpdate() {
            this.modifiedDate = LocalDateTime.now();
        }
    }
    