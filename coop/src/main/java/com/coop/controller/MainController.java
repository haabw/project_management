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

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    public static final AtomicLong TODAY_UV_COUNT = new AtomicLong(0);
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

        // UV 카운트 처리
        LocalDate last = userLastCountDate.get(username);
        if (!today.equals(last)) {
            TODAY_UV_COUNT.incrementAndGet();
            userLastCountDate.put(username, today);
        }

        // 초대 목록 조회 (초대 ID, 프로젝트명)
        List<Object[]> pendingInvitations = entityManager.createQuery(
            "SELECT pm.id, p.projectName " +
            "FROM com.coop.entity.ProjectMemberEntity pm " +
            "JOIN pm.project p JOIN pm.user u " +
            "WHERE u.username = :username AND pm.status = 'PENDING'"
        )
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
            Authentication authentication
    ) {
        String action = payload.get("action");
        Long inviteId = Long.parseLong(payload.get("projectId")); // 실제는 project_members.id
        String username = authentication.getName();

        int updated = 0;
        if ("accept".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                "UPDATE com.coop.entity.ProjectMemberEntity pm " +
                "SET pm.status = 'APPROVED' " +
                "WHERE pm.id = :id AND pm.status = 'PENDING'"
            )
            .setParameter("id", inviteId)
            .executeUpdate();
        } else if ("reject".equalsIgnoreCase(action)) {
            updated = entityManager.createQuery(
                "DELETE FROM com.coop.entity.ProjectMemberEntity pm " +
                "WHERE pm.id = :id AND pm.status = 'PENDING'"
            )
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
}
