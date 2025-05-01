package com.coop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.coop.dto.SignupDTO;
import com.coop.dto.UserView; // 🔥 추가
import com.coop.entity.ProjectMemberEntity.ProjectRole; // 🔥 추가
import com.coop.entity.UserEntity;
import com.coop.repository.UserRepository;

import java.time.LocalDateTime; // 🔥 추가
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service //서비스 계층의 컴포넌트라는 것을 명시 
public class UserService { // 사용자 관련 로직 

	// 의존성 주입
    private final UserRepository userRepository; // 유저 레포지토리 
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화를 위한 인코더 

    @PersistenceContext
    private EntityManager entityManager; // 🔥 추가

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 회원 가입 저장
    public void save(SignupDTO signupDTO) {
    	// 사용자명 중복 확인 
        if (userRepository.findByUsername(signupDTO.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        // 이메일 중복 확인 
        if (userRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        // 닉네임 중복 확인 
        if (userRepository.findByNickname(signupDTO.getNickname()).isPresent()) {
            throw new RuntimeException("이미 가입된 닉네임입니다.");
        }

        // 유저 엔티티 생성 및 데이터 설정
        UserEntity user = new UserEntity();
        user.setUsername(signupDTO.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(signupDTO.getPassword()));
        user.setEmail(signupDTO.getEmail());
        user.setNickname(signupDTO.getNickname());
        
        // 사용자 정보를 저장 
        userRepository.save(user);
    }

    // 사용자 정보 조회
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ────────────────────────────────────────────────
    // 🔥🔥🔥 팀장님 코드 여기까지 🔥🔥🔥
    // ────────────────────────────────────────────────

    // 🔥 사용자 ID로 조회
    public Optional<UserEntity> findById(int userId) {
        return userRepository.findById(userId);
    }

    // 🔥 프로젝트별 사용자 조회 (UserView 반환)
    public List<UserView> findAllMembers(int projectId) {
        List<Object[]> results = entityManager.createQuery(
            "SELECT u.id, u.nickname, u.email, pm.role, pm.id, u.createdDate " + // 🔥 createdDate 추가
            "FROM ProjectMemberEntity pm JOIN pm.user u " +
            "WHERE pm.project.id = :pid", Object[].class)
            .setParameter("pid", projectId)
            .getResultList();

        return results.stream()
            .map(row -> new UserView(
                (Integer) row[0],   // u.id
                (String)  row[1],   // u.nickname
                (String)  row[2],   // u.email
                ((ProjectRole) row[3]).name(), // pm.role
                (Integer) row[4],   // pm.id (projectMemberId)
                (LocalDateTime) row[5] // 🔥 createdDate
            ))
            .toList();
    }

    // 🔥 프로젝트 내 사용자 역할 조회
    public ProjectRole getProjectRole(int userId, int projectId) {
        return entityManager.createQuery(
            "SELECT pm.role FROM ProjectMemberEntity pm " +
            "WHERE pm.user.id = :uid AND pm.project.id = :pid", ProjectRole.class)
            .setParameter("uid", userId)
            .setParameter("pid", projectId)
            .getSingleResult();
    }

    // 🔥 프로젝트 내 사용자 권한 변경
    @Transactional // 메서드 실행을 트랜잭션 범위 내에서 처리하도록 지정
    public void changeUserRole(int projectId, int userId, ProjectRole newRole) {
        int updated = entityManager.createQuery(
            "UPDATE ProjectMemberEntity pm SET pm.role = :role " +
            "WHERE pm.project.id = :pid AND pm.user.id = :uid")
            .setParameter("role", newRole)
            .setParameter("pid", projectId)
            .setParameter("uid", userId)
            .executeUpdate();

        if (updated == 0) {
            throw new IllegalArgumentException("멤버를 찾을 수 없습니다.");
        }
    }

    // 🔥 프로젝트 내 사용자 추방 (★ 수정된 부분 ★)
    public void kickUser(int projectMemberId) {
        int deleted = entityManager.createQuery(
            "DELETE FROM ProjectMemberEntity pm " +
            "WHERE pm.id = :pmid")
            .setParameter("pmid", projectMemberId)
            .executeUpdate();

        if (deleted == 0) {
            throw new IllegalArgumentException("멤버를 찾을 수 없습니다.");
        }
    }

    // 🔥 초대 전송
    public void sendInvite(int senderId, int receiverId, int projectId) {
        entityManager.createNativeQuery(
            "INSERT INTO invite (sender_id, receiver_id, project_id, status, created_at) " +
            "VALUES (?, ?, ?, 'PENDING', NOW())")
            .setParameter(1, senderId)
            .setParameter(2, receiverId)
            .setParameter(3, projectId)
            .executeUpdate();
    }

    // 🔥 초대 대기중 여부
    public boolean hasPendingInvite(int userId) {
        Object result = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM invite WHERE receiver_id = ? AND status = 'PENDING'")
            .setParameter(1, userId)
            .getSingleResult();
        return result instanceof Number && ((Number) result).intValue() > 0;
    }
}
