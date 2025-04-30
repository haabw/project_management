package com.coop.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Index, Admin, Chat, Gantt, MindMap 라우팅
@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // 오늘 UV 카운터
    public static final AtomicLong TODAY_UV_COUNT = new AtomicLong(0);

    // 로그인 사용자별 “마지막 카운트 날짜” 저장
    private static final ConcurrentHashMap<String, LocalDate> userLastCountDate = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/index")
    public String index(Model model, Authentication authentication) {
        String username = authentication.getName();
        LocalDate today = LocalDate.now();

        // UV 집계
        LocalDate last = userLastCountDate.get(username);
        if (!today.equals(last)) {
            TODAY_UV_COUNT.incrementAndGet();
            userLastCountDate.put(username, today);
        }

        // PENDING 초대 조회 (username 필드를 기준으로 변경)
        List<Object[]> pendingInvitations = entityManager.createQuery(
                "SELECT pm.project.id, p.projectName " +
                "FROM com.coop.entity.ProjectMemberEntity pm " +
                "JOIN pm.project p JOIN pm.user u " +
                "WHERE u.username = :username AND pm.status = 'PENDING'"
        )
        .setParameter("username", username)
        .getResultList();

        // Spring 로깅 (SLF4J)
        logger.info("[{}] pendingInvitations = {}", username, pendingInvitations);

        if (!pendingInvitations.isEmpty()) {
            Object[] inv = pendingInvitations.get(0);
            model.addAttribute("inviteProjectId", ((Number) inv[0]).longValue());
            model.addAttribute("inviteProjectName", inv[1]);
        }

        return "index";
    }

    @PostMapping("/api/invitation/response")
    @Transactional
    public ResponseEntity<Map<String, Object>> handleInvitationResponse(
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        String action = payload.get("action");
        Long projectId = Long.parseLong(payload.get("projectId"));
        String username = authentication.getName();

        int updated = 0;
        if ("accept".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                    "UPDATE com.coop.entity.ProjectMemberEntity pm " +
                    "SET pm.status = 'APPROVED' " +
                    "WHERE pm.user.username = :username " +
                    "AND pm.project.id = :projectId " +
                    "AND pm.status = 'PENDING'"
            )
            .setParameter("username", username)
            .setParameter("projectId", projectId)
            .executeUpdate();
        } else if ("reject".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                    "DELETE FROM com.coop.entity.ProjectMemberEntity pm " +
                    "WHERE pm.user.username = :username " +
                    "AND pm.project.id = :projectId " +
                    "AND pm.status = 'PENDING'"
            )
            .setParameter("username", username)
            .setParameter("projectId", projectId)
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
}
