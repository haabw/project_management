package com.coop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler; // AuthenticationFailureHandler import 추가
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.coop.service.CustomUserDetailsService;

/**
 * 스프링 시큐리티 설정 클래스입니다.
 * 웹 애플리케이션의 인증, 인가, 로그인, 로그아웃 처리 및 비밀번호 암호화 등을 구성합니다.
 */
@Configuration // 이 클래스가 스프링 설정 클래스임을 나타냅니다.
public class SecurityConfig {

    // 사용자 정보를 로드하는 서비스
    private final CustomUserDetailsService userDetailsService;
    // 로그인 실패 시 처리를 담당하는 핸들러
    private final AuthenticationFailureHandler customAuthFailureHandler;

    /**
     * 필요한 의존성을 주입받는 생성자입니다.
     * @param userDetailsService 사용자 상세 정보 서비스
     * @param customAuthFailureHandler 로그인 실패 핸들러
     */
    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, AuthenticationFailureHandler customAuthFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthFailureHandler = customAuthFailureHandler; // feature-branch의 핸들러 주입 포함
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈을 등록합니다.
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 보안 설정을 구성하는 SecurityFilterChain 빈을 등록합니다.
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth // HTTP 요청에 대한 인증 및 인가 규칙을 설정합니다.
                .requestMatchers( // 특정 URL 패턴에 대한 접근 권한을 설정합니다.
                    new AntPathRequestMatcher("/"), // 루트 경로 ("/")
                    new AntPathRequestMatcher("/auth/login"), // 로그인 페이지 ("/auth/login")
                    new AntPathRequestMatcher("/auth/signup"), // 회원가입 페이지 ("/auth/signup")
                    new AntPathRequestMatcher("/css/**"), // CSS 파일 경로 ("/css/**")
                    new AntPathRequestMatcher("/js/**"), // JavaScript 파일 경로 ("/js/**")
                    new AntPathRequestMatcher("/images/**"), // 이미지 파일 경로 ("/images/**")
                    new AntPathRequestMatcher("/error/**") // 에러 페이지 경로 ("/error/**")
                ).permitAll() // 위에 명시된 경로들은 인증 없이 누구나 접근할 수 있도록 허용합니다.
                // .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN") // 예: 관리자 경로 설정 (필요시 주석 해제)
                .anyRequest().authenticated() // 그 외의 모든 요청은 반드시 인증된 사용자만 접근할 수 있도록 설정합니다.
            )
            .formLogin(form -> form // 폼 기반 로그인을 설정합니다.
                .loginPage("/auth/login") // 사용자 정의 로그인 페이지 경로를 지정합니다.
                .defaultSuccessUrl("/index") // 로그인 성공 시 리다이렉트될 기본 URL을 지정합니다.
                .failureHandler(customAuthFailureHandler) // 로그인 실패 시 처리를 담당할 핸들러를 지정합니다. (feature-branch의 설정 유지)
                .usernameParameter("username") // 로그인 폼에서 사용자 이름(아이디) 파라미터의 이름을 지정합니다.
                .passwordParameter("password") // 로그인 폼에서 비밀번호 파라미터의 이름을 지정합니다.
            )
            .logout(logout -> logout // 로그아웃 설정을 구성합니다.
                .logoutSuccessUrl("/auth/login") // 로그아웃 성공 시 리다이렉트될 URL을 지정합니다.
                .invalidateHttpSession(true) // 로그아웃 시 HTTP 세션을 무효화합니다.
                .deleteCookies("JSESSIONID") // 로그아웃 시 쿠키(예: 세션 ID)를 삭제합니다. (선택 사항)
            )
            .csrf(AbstractHttpConfigurer::disable); // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다. (개발/테스트 환경에서는 편리하지만, 운영 환경에서는 활성화 고려)
            // .userDetailsService(userDetailsService); // CustomUserDetailsService를 사용하도록 명시 (필요한 경우 주석 해제)

        return http.build(); // 구성된 HttpSecurity 설정을 기반으로 SecurityFilterChain 객체를 생성하여 반환합니다.
    }
}
