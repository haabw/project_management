// 기존 코드 유지
package com.coop.controller;

import com.coop.dto.UserView;
import com.coop.entity.ChatEntity;
import com.coop.entity.ProjectEntity;
import com.coop.entity.UserEntity;
import com.coop.entity.ProjectMemberEntity.ProjectRole;
import com.coop.repository.ChatRepository;
import com.coop.repository.UserRepository;
import com.coop.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.coop.controller.MainController.TODAY_UV_COUNT;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminController(UserService userService, UserRepository userRepository, ChatRepository chatRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    // ✅ 채팅 이력 섹션 요청 처리 (필터링 및 페이지 기반)
    @GetMapping(params = {"section=chat", "projectId"})
    public String showChatLogs(
            @RequestParam("projectId") Integer projectId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model,
            Authentication authentication
    ) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("로그인 사용자 없음"));

        ProjectEntity project = entityManager.find(ProjectEntity.class, projectId);
        if (project == null) throw new RuntimeException("해당 프로젝트가 존재하지 않습니다.");

        int pageSize = 20;
        int offset = (page - 1) * pageSize;

        StringBuilder baseQuery = new StringBuilder("FROM ChatEntity c WHERE c.project = :project");
        Map<String, Object> params = new HashMap<>();
        params.put("project", project);

        if (fromDate != null && !fromDate.isBlank()) {
            baseQuery.append(" AND c.timestamp >= :fromDate");
            params.put("fromDate", LocalDate.parse(fromDate).atStartOfDay());
        }
        if (toDate != null && !toDate.isBlank()) {
            baseQuery.append(" AND c.timestamp <= :toDate");
            params.put("toDate", LocalDate.parse(toDate).atTime(23, 59, 59));
        }
        if (category != null && !category.isBlank()) {
            baseQuery.append(" AND c.category = :category");
            params.put("category", category);
        }
        if (keyword != null && !keyword.isBlank()) {
            baseQuery.append(" AND (c.message LIKE :keyword OR c.user.nickname LIKE :keyword)");
            params.put("keyword", "%" + keyword + "%");
        }

        String countQuery = "SELECT COUNT(c) " + baseQuery.toString();
        var countQ = entityManager.createQuery(countQuery, Long.class);
        params.forEach(countQ::setParameter);
        Long totalCount = countQ.getSingleResult();
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);

        String selectQuery = "SELECT c " + baseQuery.toString() + " ORDER BY c.timestamp DESC";
        var query = entityManager.createQuery(selectQuery, ChatEntity.class)
                .setFirstResult(offset)
                .setMaxResults(pageSize);
        params.forEach(query::setParameter);

        List<ChatEntity> chatLogs = query.getResultList();

        model.addAttribute("chatLogs", chatLogs);
        model.addAttribute("section", "chat");
        model.addAttribute("projectId", projectId);
        model.addAttribute("currentUsername", username);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        // ✅ 검색 조건도 모델에 포함 (👉 이 줄들을 추가!)
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);


        return "admin";
    }

    /**
     * 관리자 페이지 진입 시 처리되는 메인 로직
     * - projectId 없으면 자동으로 내 프로젝트로 redirect
     * - 현재 로그인 유저가 해당 프로젝트의 ADMIN인지 검증
     * - 초대 대기자, 승인자 목록 전달
     * - 통계 데이터 구성 후 admin.html로 전달
     */
    @GetMapping
    public String adminHome(
            @RequestParam(value = "section", defaultValue = "dashboard") String section,
            @RequestParam(value = "projectId", required = false) Integer projectId,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        Optional<UserEntity> optionalUser = userService.findByUsername(currentUsername);
        if (optionalUser.isEmpty()) return "redirect:/login";
        UserEntity currentUser = optionalUser.get();

        if (projectId == null) {
            @SuppressWarnings("unchecked")
            List<Integer> myProjectIds = entityManager.createNativeQuery(
                "SELECT project_id FROM project_members WHERE user_id = :uid AND status = 'APPROVED'")
                .setParameter("uid", currentUser.getId())
                .getResultList();

            if (!myProjectIds.isEmpty()) {
                Integer firstProjectId = myProjectIds.get(0);
                return "redirect:/admin?section=" + section + "&projectId=" + firstProjectId;
            } else {
                model.addAttribute("noProject", true);
                return "admin";
            }
        }

        model.addAttribute("section", section);
        model.addAttribute("projectId", projectId);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentUserId", currentUser.getId());
        // 현재 유저가 ADMIN인지 검사
        ProjectRole role = userService.getProjectRole(currentUser.getId(), projectId);
        if (role == null || role != ProjectRole.ADMIN) {
            return "redirect:/index";
        }
        model.addAttribute("currentUserRole", role.name());
        // 팀원 관리/권한 설정 섹션일 경우: 멤버 정보 구성
        if ("members".equals(section) || "permissions".equals(section)) {
            List<UserView> fetched = Optional.ofNullable(userService.findAllMembers(projectId)).orElse(Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<Integer> pendingUserIds = entityManager.createNativeQuery(
                "SELECT user_id FROM project_members WHERE project_id = :pid AND status = 'PENDING'")
                .setParameter("pid", projectId)
                .getResultList();
            // 승인된 멤버만 필터링
            @SuppressWarnings("unchecked")
            List<Integer> approvedUserIds = entityManager.createNativeQuery(
                "SELECT user_id FROM project_members WHERE project_id = :pid AND status = 'APPROVED'")
                .setParameter("pid", projectId)
                .getResultList();

            List<UserView> approvedMembers = fetched.stream()
                .filter(u -> approvedUserIds.contains(u.getId()))
                .collect(Collectors.toList());

            model.addAttribute("users", approvedMembers);
            model.addAttribute("pendingUserIds", pendingUserIds);
            model.addAttribute("approvedUserIds", approvedUserIds);
        } else {
            model.addAttribute("users", Collections.emptyList());
            model.addAttribute("pendingUserIds", Collections.emptyList());
            model.addAttribute("approvedUserIds", Collections.emptyList());
        }
        // 나에게 온 초대 목록 (PENDING 상태)
        @SuppressWarnings("unchecked")
        List<Object[]> pendingRaw = entityManager.createNativeQuery(
            "SELECT pm.id, p.project_name FROM project_members pm " +
            "JOIN project p ON pm.project_id = p.project_id " +
            "WHERE pm.user_id = :uid AND pm.status = 'PENDING'")
            .setParameter("uid", currentUser.getId())
            .getResultList();
        model.addAttribute("pendingRaw", pendingRaw);
        // 방문자 수 (금일 접속자 수 포함)
        long todayUv = TODAY_UV_COUNT.get();
        model.addAttribute("todayUv", todayUv);
        // 최근 7일 방문자 통계
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        Map<String, Long> manualStats = Map.of(
            startDate.format(formatter), 2L,
            startDate.plusDays(1).format(formatter), 5L,
            startDate.plusDays(2).format(formatter), 3L,
            startDate.plusDays(3).format(formatter), 7L,
            startDate.plusDays(4).format(formatter), 6L,
            startDate.plusDays(5).format(formatter), 8L
        );

        LinkedHashMap<String, Long> finalStats = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String key = date.format(formatter);
            long count = date.equals(today) ? todayUv : manualStats.getOrDefault(key, 0L);
            finalStats.put(key, count);
        }

        model.addAttribute("uvLabels", new ArrayList<>(finalStats.keySet()));
        model.addAttribute("uvCounts", new ArrayList<>(finalStats.values()));

        return "admin";
    }

    // ✅ index 전용 초대 알림용 GET
    @GetMapping("/index")
    public String userIndexWithInvitation(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<UserEntity> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) return "redirect:/login";

        UserEntity user = userOpt.get();

        @SuppressWarnings("unchecked")
        List<Object[]> pendingRaw = entityManager.createNativeQuery(
            "SELECT pm.id, p.project_name FROM project_members pm " +
            "JOIN project p ON pm.project_id = p.project_id " +
            "WHERE pm.user_id = :uid AND pm.status = 'PENDING'")
            .setParameter("uid", user.getId())
            .getResultList();

        model.addAttribute("username", user.getNickname());
        model.addAttribute("pendingRaw", pendingRaw);
        model.addAttribute("section", "invitation"); // ✅ admin.html에서 초대 섹션 활성화

        return "admin";
    }
    /**
     * 권한 변경 처리
     */
    @PostMapping("/permissions")
    public ResponseEntity<String> changeUserRole(
            @RequestParam("projectId") int projectId,
            @RequestParam("userId") int userId,
            @RequestParam("role") String role
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        UserEntity currentUser = userService.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("현재 사용자 정보를 찾을 수 없습니다."));
        ProjectRole currentRole = userService.getProjectRole(currentUser.getId(), projectId);
        if (currentRole == ProjectRole.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
        }
        userService.changeUserRole(projectId, userId, ProjectRole.valueOf(role));
        return ResponseEntity.ok("권한 변경 완료");
    }
    /**
     * 팀원 추방 처리
     */
    @PostMapping("/kick")
    @Transactional
    public ResponseEntity<String> kickUser(@RequestParam("projectMemberId") int projectMemberId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        userService.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("현재 사용자 정보를 찾을 수 없습니다."));
        userService.kickUser(projectMemberId);
        return ResponseEntity.ok("사용자 추방 완료");
    }

    /**
     * 초대 전송 처리 (중복 초대 방지 포함)
     */
    @PostMapping("/invite/send")
    @Transactional
    public ResponseEntity<String> sendProjectInvite(
            @RequestParam("receiverId") int receiverId,
            @RequestParam("projectId") int projectId
    ) {
    	   Long count = ((Number) entityManager.createNativeQuery(
    		        "SELECT COUNT(*) FROM project_members WHERE user_id = :uid AND project_id = :pid AND status = 'PENDING'")
    		        .setParameter("uid", receiverId)
    		        .setParameter("pid", projectId)
    		        .getSingleResult()).longValue();

    		    if (count > 0) {
    		        return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 초대된 사용자입니다.");
    		    }

        entityManager.createNativeQuery(
            "INSERT INTO project_members (user_id, project_id, role, status) " +
            "VALUES (:userId, :projectId, 'USER', 'PENDING')")
            .setParameter("userId", receiverId)
            .setParameter("projectId", projectId)
            .executeUpdate();
        return ResponseEntity.ok("초대 전송 완료");
    }
    /**
     * 초대 가능한 사용자 목록 조회 (JSON 반환)
     */
    @GetMapping("/invite/users")
    @ResponseBody
    public List<UserView> getUsersForInvite() {
        return userRepository.findAll().stream()
            .map(u -> new UserView(u.getId(), u.getNickname(), u.getEmail(), null, null, u.getCreatedDate()))
            .collect(Collectors.toList());
    }
    /**
     * 초대 수락 처리
     */
    @PostMapping("/invite/accept")
    @Transactional
    public ResponseEntity<String> acceptProjectInvite(@RequestParam("inviteId") int inviteId) {
        try {
            int updated = entityManager.createNativeQuery(
                "UPDATE project_members SET status = 'APPROVED' WHERE id = :inviteId AND status = 'PENDING'")
                .setParameter("inviteId", inviteId)
                .executeUpdate();
            if (updated == 0) {
                return ResponseEntity.badRequest().body("초대가 존재하지 않거나 이미 처리되었습니다.");
            }
            return ResponseEntity.ok("프로젝트에 성공적으로 참여하셨습니다!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("초대 수락 중 오류가 발생했습니다.");
        }
    }

    /**
     * 초대 거절 처리
     */
    @PostMapping("/invite/decline")
    @Transactional
    public ResponseEntity<String> declineProjectInvite(@RequestParam("inviteId") int inviteId) {
        try {
            int deleted = entityManager.createNativeQuery(
                "DELETE FROM project_members WHERE id = :inviteId AND status = 'PENDING'")
                .setParameter("inviteId", inviteId)
                .executeUpdate();
            if (deleted == 0) {
                return ResponseEntity.badRequest().body("초대가 존재하지 않거나 이미 처리되었습니다.");
            }
            return ResponseEntity.ok("초대 거절 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("초대 거절 중 오류가 발생했습니다.");
        }
    }
    /**
     * 프로젝트 생성 및 ADMIN 등록
     */
    @PostMapping("/project/create")
    @Transactional
    public ResponseEntity<String> createProject(@RequestParam("projectName") String projectName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        UserEntity currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 1. 프로젝트 생성
        entityManager.createNativeQuery(
                "INSERT INTO project (project_name, create_date) VALUES (:pname, NOW())")
                .setParameter("pname", projectName)
                .executeUpdate();

        // 2. 방금 생성된 프로젝트 ID 가져오기
        Integer projectId = (Integer) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();

        // 3. 프로젝트 생성자를 ADMIN으로 등록
        entityManager.createNativeQuery(
                "INSERT INTO project_members (user_id, project_id, role, status) " +
                        "VALUES (:uid, :pid, 'ADMIN', 'APPROVED')")
                .setParameter("uid", currentUser.getId())
                .setParameter("pid", projectId)
                .executeUpdate();

        return ResponseEntity.ok("프로젝트 생성 및 관리자 등록 완료");
    }  
}
