package com.coop.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;      // ← 추가된 import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coop.dto.ProjectDTO;
import com.coop.entity.ProjectEntity;                    // ← 추가된 import
import com.coop.entity.ProjectMemberEntity;              // ← 추가된 import
import com.coop.entity.ProjectMemberEntity.ProjectRole;  // ← 추가된 import
import com.coop.entity.ProjectMemberEntity.ProjectStatus;// ← 추가된 import
import com.coop.entity.UserEntity;                       // ← 추가된 import
import com.coop.service.ProjectService;
import com.coop.service.UserService;                     // ← 추가된 import

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    public static final AtomicLong TODAY_UV_COUNT = new AtomicLong(0);
    private static final ConcurrentHashMap<String, LocalDate> userLastCountDate = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    // ─── 기존에 없던 UserService 주입 ───────────────────────────
    private final ProjectService projectService;
    private final UserService userService;

    @Autowired
    public MainController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/index")
    public String index(Model model, Authentication authentication) {
        String username = authentication.getName();
        LocalDate today = LocalDate.now();

        // UV 카운트 처리
        LocalDate last = userLastCountDate.get(username);
        if (!today.equals(last)) {
            TODAY_UV_COUNT.incrementAndGet();
            userLastCountDate.put(username, today);
        }

        // 초대 목록 조회 (초대 ID, 프로젝트명)
        List<Object[]> pendingInvitations = entityManager
                .createQuery(
                    "SELECT pm.id, p.projectName " +
                    "FROM com.coop.entity.ProjectMemberEntity pm " +
                    "JOIN pm.project p JOIN pm.user u " +
                    "WHERE u.username = :username AND pm.status = 'PENDING'")
                .setParameter("username", username)
                .getResultList();

        logger.info("[{}] pendingInvitations = {}", username, pendingInvitations);

        // ✅ 전체 초대 목록 모델에 전달
        model.addAttribute("pendingRaw", pendingInvitations);

        // ✅ 첫 초대 정보도 유지 (선택사항)
        if (!pendingInvitations.isEmpty()) {
            Object[] first = pendingInvitations.get(0);
            model.addAttribute("inviteProjectId", ((Number) first[0]).longValue());
            model.addAttribute("inviteProjectName", first[1]);
        }

        model.addAttribute("username", username);
        return "index"; // index.html 렌더링
    }

    @PostMapping("/api/invitation/response")
    @Transactional
    public ResponseEntity<Map<String, Object>> handleInvitationResponse(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        String action = payload.get("action");
        Long inviteId = Long.parseLong(payload.get("projectId")); // 실제는 project_members.id
        String username = authentication.getName();

        int updated = 0;
        if ("accept".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                    "UPDATE com.coop.entity.ProjectMemberEntity pm " +
                    "SET pm.status = 'APPROVED' " +
                    "WHERE pm.id = :id AND pm.status = 'PENDING'")
                .setParameter("id", inviteId)
                .executeUpdate();
        } else if ("reject".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                    "DELETE FROM com.coop.entity.ProjectMemberEntity pm " +
                    "WHERE pm.id = :id AND pm.status = 'PENDING'")
                .setParameter("id", inviteId)
                .executeUpdate();
        }

        Map<String, Object> response = new HashMap<>();
        if (updated > 0) {
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "초대 응답 처리 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ─── 프로젝트 관련: 생성 시 생성자 자동 ADMIN·APPROVED 등록 ───────────────────────────
    @PostMapping("/projects/add")
    @ResponseBody
    @Transactional
    public ProjectDTO add(@RequestBody ProjectDTO projectDTO, Authentication authentication) {
        // 1) 프로젝트 저장
        ProjectDTO created = projectService.createProject(projectDTO);

        // 2) 로그인 사용자 조회
        String username = authentication.getName();
        UserEntity owner = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다."));

        // 3) 엔티티 매니저로 방금 생성된 ProjectEntity 조회
        ProjectEntity project = entityManager.find(ProjectEntity.class, created.getProjectId());

        // 4) project_members에 ADMIN·APPROVED로 멤버 등록
        ProjectMemberEntity pm = new ProjectMemberEntity();
        pm.setUser(owner);
        pm.setProject(project);
        pm.setRole(ProjectRole.ADMIN);
        pm.setStatus(ProjectStatus.APPROVED);
        entityManager.persist(pm);

        return created;
    }

    @GetMapping("/projects/list")
    @ResponseBody
    public List<ProjectDTO> list() {
        return projectService.getAllProjects();
    }

    @PutMapping("/projects/update")
    @ResponseBody
    public ProjectDTO update(@RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(projectDTO.getProjectId(), projectDTO);
    }

    @DeleteMapping("/projects/delete/{id}")
    @ResponseBody
    public void delete(@PathVariable int id) {
        projectService.deleteProject(id);
    }
}
