package com.coop.controller;

import com.coop.dto.UserView;
import com.coop.entity.UserEntity;
import com.coop.entity.ProjectMemberEntity.ProjectRole;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

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

        // 자동 리디렉션 처리
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

        ProjectRole role = userService.getProjectRole(currentUser.getId(), projectId);
        model.addAttribute("currentUserRole", role.name());

        // 초대 대기/승인 사용자 목록
        if ("members".equals(section) || "permissions".equals(section)) {
            List<UserView> fetched = Optional.ofNullable(userService.findAllMembers(projectId)).orElse(Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<Integer> pendingUserIds = entityManager.createNativeQuery(
                "SELECT user_id FROM project_members WHERE project_id = :pid AND status = 'PENDING'")
                .setParameter("pid", projectId)
                .getResultList();

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

        @SuppressWarnings("unchecked")
        List<Object[]> pendingRaw = entityManager.createNativeQuery(
            "SELECT pm.id, p.project_name FROM project_members pm " +
            "JOIN projects p ON pm.project_id = p.project_id " +
            "WHERE pm.user_id = :uid AND pm.status = 'PENDING'")
            .setParameter("uid", currentUser.getId())
            .getResultList();
        model.addAttribute("pendingRaw", pendingRaw);

        // 방문자 통계 처리
        long todayUv = TODAY_UV_COUNT.get();
        model.addAttribute("todayUv", todayUv);

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

    @PostMapping("/invite/send")
    @Transactional
    public ResponseEntity<String> sendProjectInvite(
            @RequestParam("receiverId") int receiverId,
            @RequestParam("projectId") int projectId
    ) {
        entityManager.createNativeQuery(
            "INSERT INTO project_members (user_id, project_id, role, status) " +
            "VALUES (:userId, :projectId, 'USER', 'PENDING')")
            .setParameter("userId", receiverId)
            .setParameter("projectId", projectId)
            .executeUpdate();
        return ResponseEntity.ok("초대 전송 완료");
    }

    @GetMapping("/invite/users")
    @ResponseBody
    public List<UserView> getUsersForInvite() {
        return userRepository.findAll().stream()
            .map(u -> new UserView(u.getId(), u.getNickname(), u.getEmail(), null, null, u.getCreatedDate()))
            .collect(Collectors.toList());
    }

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
            return ResponseEntity.ok("초대 수락 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("초대 수락 중 오류가 발생했습니다.");
        }
    }

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
}
