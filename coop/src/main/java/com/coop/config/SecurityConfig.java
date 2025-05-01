package com.coop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.coop.service.CustomUserDetailsService;

// 스프링 시큐리티 설정 클래스
// 인증, 인가, 로그인, 로그아웃, 비밀번호 암호화 등을 구성함
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth // 인증, 인가 설정
                    // --- 모든 요청 허용 (테스트용) ---
                    // 주의: 테스트 완료 후 반드시 원래 보안 규칙으로 복구해야 합니다!
                    .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                    /* --- 원래 보안 규칙 (참고용 주석) ---
                    .requestMatchers( // 특정 url 엑세스 설정
                            new AntPathRequestMatcher("/"), // 루트 경로
                            new AntPathRequestMatcher("/auth/login"), // 로그인 페이지
                            new AntPathRequestMatcher("/auth/signup"), // 회원가입 페이지
                            new AntPathRequestMatcher("/css/**"), // css 파일
                            new AntPathRequestMatcher("/js/**"), // js 파일
                            new AntPathRequestMatcher("/images/**"), // 이미지 파일
                            new AntPathRequestMatcher("/error/**") // 에러 페이지
                            ).permitAll() // 위 요청이 오면 인증, 인가 없이 접근 가능
                    // .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN") // 관리자만 접근 가능 (필요시 주석 해제)
                    .anyRequest().authenticated() // 그 외 페이지는 모두 인증 필요
                    */
            )
            .formLogin(form -> form // 폼 기반 로그인 설정
                    .loginPage("/auth/login")  // 로그인 페이지 경로 설정
                    .defaultSuccessUrl("/index") // 로그인 성공 시 이동 경로
                    .usernameParameter("username") // 사용자 이름 파라미터명
                    .passwordParameter("password") // 비밀번호 파라미터명
            )
            .logout(logout -> logout
                    .logoutSuccessUrl("/auth/login") // 로그아웃 성공 시 이동할 경로
                    .invalidateHttpSession(true)  // 로그아웃 후 세션 무효화
            )
            .csrf(AbstractHttpConfigurer::disable); // CSRF 보호 비활성화 (개발/테스트 시 편리)

        return http.build();
    }
}