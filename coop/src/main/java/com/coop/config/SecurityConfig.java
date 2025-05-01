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

	// userDetailsService 사용 추가 
	private final CustomUserDetailsService userDetailsService;
	
	@Autowired
	public SecurityConfig(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
	
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // 스프링 시큐리티의 보안 필터 체인
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth //인증, 인가 설정 
            		.requestMatchers( //특정 url 엑세스 설정 
            				new AntPathRequestMatcher("/"), //루트 경로 
            				new AntPathRequestMatcher("/auth/login"), // 로그인 페이지 
            				new AntPathRequestMatcher("/auth/signup"), // 회원가입 페이지 
            				new AntPathRequestMatcher("/css/**"), // css파일 
            			    new AntPathRequestMatcher("/js/**"), // js파일 
            			    new AntPathRequestMatcher("/images/**"), //이미지파일 
            			    new AntPathRequestMatcher("/error/**") // 에러 페이지 
            				).permitAll() //위 요청이 오면 인증, 인가 없이 접근 가능 
            				.anyRequest().authenticated() // 그 외 페이지는 모두 인증 필요 
            				)
            .formLogin(form -> form //폼 기반 로그인 설정 
            		.loginPage("/auth/login")  //로그인 페이지 경로 설정 
            		.defaultSuccessUrl("/index") //로그인 시 이동 경로 
            		.usernameParameter("username")
            		.passwordParameter("password")
            		)
            .logout(logout -> logout
            		.logoutSuccessUrl("/auth/login") //로그아웃 시 이동할 경로 
            		.invalidateHttpSession(true)  //로그아웃 후 세션을 삭제할지 여부
            		)
            .csrf(AbstractHttpConfigurer::disable); //테스트를 위해 csrf 비활성화
        return http.build();
    }
}