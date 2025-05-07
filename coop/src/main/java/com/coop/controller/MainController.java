package com.coop.controller;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam; // 추가!
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
import jakarta.persistence.EntityNotFoundException; // <--- EntityNotFoundException 임포트 추가!
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
        public List<ProjectDTO> list(Authentication authentication) {
    	// 1) 로그인한 사용자 이름 가져오기 (한 번만 선언)
    	    String username = authentication.getName();
    	    
    	    // 2) 서비스에 username 넘겨서 조회
    	    return projectService.getMyApprovedProjects(username);
    }

    /** 프로젝트 이름 수정 */
    @PutMapping("/projects/update")
    @ResponseBody
    public ResponseEntity<ProjectDTO> updateProject(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {

        // 1) 로그인 사용자 조회
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("로그인된 사용자 정보가 없습니다."));

        // 2) 요청 데이터 파싱
        int projectId = Integer.parseInt(payload.get("projectId"));
        String newName  = payload.get("projectName");

        // 3) 수정 (Service 레벨에서 권한 체크 추가 가능)
        ProjectDTO updated = projectService.updateProjectName(projectId, newName);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/projects/delete/{id}")
    @ResponseBody
    public void delete(@PathVariable int id) {
        projectService.deleteProject(id);
    }
    /**
     * 채팅 페이지 요청을 처리하는 메서드 (Request Parameter 사용 - 최종 수정본)
     * 경로: /chat
     * @param projectId URL 파라미터에서 받아온 프로젝트 ID (?projectId=...)
     * @param model     View(chat.html)에 데이터를 전달하기 위한 객체
     * @param authentication Spring Security가 제공하는 현재 사용자 인증 정보
     * @return 보여줄 템플릿 이름 ("chat") 또는 에러 페이지
     */
    @GetMapping("/chat") // 요청 경로
    public String chatPageByParam(@RequestParam("projectId") Integer projectId, Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            // 로그인되지 않은 사용자는 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        try {
            // 1. 현재 로그인된 사용자 정보 가져오기 (DB 조회)
            String currentLoginId = authentication.getName(); // 로그인 시 사용한 ID (username)
            UserEntity currentUser = userService.findByUsername(currentLoginId)
                     // 사용자를 찾지 못하면 EntityNotFoundException 발생
                     .orElseThrow(() -> new EntityNotFoundException("로그인 사용자를 찾을 수 없습니다: " + currentLoginId));

            // 2. 프로젝트 정보 가져오기 (DB 조회 - ProjectService 경유)
            // ProjectService 내부에서 projectRepository.findById(id)를 사용할 것으로 예상
            ProjectEntity project = projectService.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID: " + projectId));

            // [보안 강화 제안] TODO: 현재 로그인한 사용자가 이 프로젝트의 멤버인지 확인하는 로직 추가
            // boolean isMember = projectService.isUserMemberOfProject(currentUser.getId(), projectId);
            // if (!isMember) {
            //     throw new AccessDeniedException("해당 프로젝트에 접근 권한이 없습니다.");
            // }

            // 3. Model 객체에 View(chat.html) 렌더링에 필요한 데이터 담기
            model.addAttribute("projectId", projectId);          // 프로젝트 ID
            model.addAttribute("userId", currentUser.getId());     // 현재 사용자 ID (PK)
            model.addAttribute("username", currentUser.getNickname()); // 현재 사용자 닉네임
            model.addAttribute("project", project);              // 프로젝트 정보 객체 (이름 등 표시용)

            // 4. chat.html 템플릿 반환
            return "chat";

        } catch (EntityNotFoundException e) {
            // 사용자를 찾지 못하거나 프로젝트를 찾지 못한 경우 (404 Not Found 와 유사)
            System.err.println("채팅 페이지 접근 오류: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404"; // templates/error/404.html 반환 (경로 확인 필요)
        } catch (Exception e) {
             // 기타 예상치 못한 서버 오류
             System.err.println("채팅 페이지 로딩 중 오류 발생: " + e.getMessage());
             e.printStackTrace(); // 개발 중 상세 스택 트레이스 출력
             model.addAttribute("errorMessage", "채팅 페이지 로딩 중 오류가 발생했습니다.");
             return "error/500"; // templates/error/500.html 반환 (경로 확인 필요)
        }
    }

}
