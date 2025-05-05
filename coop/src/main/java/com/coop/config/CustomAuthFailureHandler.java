package com.coop.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 빈칸 검증
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String errorMessage;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.\n 다시 확인해 주세요.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "내부적으로 발생한 시스템 문제로 인해 요청을 처리할 수 없습니다.\n 관리자에게 문의하세요.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "계정이 존재하지 않습니다.\n 회원가입 진행 후 로그인 해주세요.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = "인증 요청이 거부되었습니다.\n 관리자에게 문의하세요.";
        } else {
            errorMessage = "알 수 없는 이유로 로그인에 실패하였습니다.\n 관리자에게 문의하세요.";
        }

        // 오류 메시지를 URL 파라미터로 인코딩하여 리다이렉트
        errorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String redirectUrl = "/auth/login?error=true&errorMessage=" + errorMessage;

        // 리다이렉트
        response.sendRedirect(redirectUrl);
    }
}