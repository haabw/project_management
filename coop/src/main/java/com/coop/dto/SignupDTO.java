package com.coop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class SignupDTO {
    @NotBlank(message = "아이디를 입력하세요.")
    private String username;
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;
    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordConfirm;
    @Email(message = "유효한 이메일을 입력하세요.")
    @NotBlank(message = "이메일을 입력하세요.")
    private String email;
    @NotBlank(message = "닉네임을 입력하세요.")
    private String nickname;

    // 비밀번호 복잡성 패턴
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+-=\\[\\]{}|;:,.<>?]).{8,}$"
    );

    public void validatePassword() {
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "비밀번호는 8자 이상이어야 하며, 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
            );
        }
    }
}