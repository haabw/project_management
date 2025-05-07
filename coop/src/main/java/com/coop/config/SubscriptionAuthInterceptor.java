package com.coop.config;

import com.coop.entity.ProjectEntity;
import com.coop.entity.ProjectMemberEntity;
import com.coop.entity.UserEntity;
import com.coop.repository.ProjectMemberRepository;
import com.coop.repository.ProjectRepository;
import com.coop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionAuthInterceptor implements ChannelInterceptor {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    private static final Pattern chatTopicPattern = Pattern.compile("/topic/chat/project/(\\d+)");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal userPrincipal = accessor.getUser();
            String destination = accessor.getDestination();

            if (userPrincipal == null) {
                log.warn("구독 시도: 인증되지 않은 사용자입니다. 목적지: {}", destination);
                throw new AccessDeniedException("인증되지 않은 사용자는 구독할 수 없습니다.");
            }

            String usernameFromPrincipal = userPrincipal.getName();
            UserEntity currentUser;
            try {
                currentUser = userService.findByUsername(usernameFromPrincipal)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + usernameFromPrincipal));
            } catch (UsernameNotFoundException e) {
                log.error("Principal로부터 사용자 정보를 가져오는 데 실패했습니다: " + usernameFromPrincipal, e);
                throw new AccessDeniedException("사용자 정보를 확인할 수 없습니다.", e);
            } catch (Exception e) {
                log.error("Principal로부터 사용자 정보 조회 중 알 수 없는 오류: " + usernameFromPrincipal, e);
                throw new AccessDeniedException("사용자 정보 확인 중 오류 발생.", e);
            }


            if (destination != null) {
                Matcher matcher = chatTopicPattern.matcher(destination);
                if (matcher.matches()) {
                    String projectIdStr = matcher.group(1);
                    try {
                        Integer projectId = Integer.parseInt(projectIdStr);
                        // ProjectEntity를 조회할 때 .orElseThrow() 내에서 AccessDeniedException 사용 고려
                        ProjectEntity project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new AccessDeniedException("구독하려는 프로젝트(" + projectIdStr + ")를 찾을 수 없습니다."));

                        boolean isMember = projectMemberRepository.existsByUserAndProjectAndStatus(
                                currentUser,
                                project,
                                ProjectMemberEntity.ProjectStatus.APPROVED
                        );

                        if (!isMember) {
                            log.warn("권한 없는 구독 시도: 사용자 {} (ID: {})가 프로젝트 '{}' ({})의 멤버가 아닙니다. 목적지: {}",
                                    currentUser.getUsername(), currentUser.getId(), project.getProjectName(), projectId, destination); // 수정: getProjectName() 사용
                            // 원하는 특정 메시지로 예외 발생
                            throw new AccessDeniedException("프로젝트 '" + project.getProjectName() + "' 채팅 채널을 구독할 권한이 없습니다."); // 수정: getProjectName() 사용
                        }
                        log.info("사용자 {} (ID: {}) 프로젝트 '{}' ({}) 구독 승인됨. 목적지: {}",
                                currentUser.getUsername(), currentUser.getId(), project.getProjectName(), projectId, destination); // 수정: getProjectName() 사용

                    } catch (NumberFormatException e) {
                        log.error("구독 경로에서 프로젝트 ID 파싱 오류: {}. 전체 목적지: {}", e.getMessage(), destination, e);
                        throw new AccessDeniedException("잘못된 형식의 채팅 채널 경로입니다. 관리자에게 문의하세요.");
                    } catch (AccessDeniedException ade) { // AccessDeniedException을 먼저 캐치하여 그대로 다시 던집니다.
                        log.warn("구독 접근 거부: {}", ade.getMessage());
                        throw ade; // 기존 예외 메시지 유지
                    } catch (RuntimeException e) { // 그 외 다른 RuntimeException 처리 (예: projectRepository.findById에서 다른 예외 발생 시)
                        log.error("구독 권한 확인 중 예상치 못한 런타임 오류 (프로젝트 ID 문자열: {}): {}", projectIdStr, e.getMessage(), e);
                        // 보다 구체적인 오류를 알 수 없는 경우 일반적인 메시지 사용
                        throw new AccessDeniedException("채팅 채널 정보를 확인하는 중 서버 내부 오류가 발생했습니다.", e);
                    }
                }
            }
        }
        return message;
    }
}