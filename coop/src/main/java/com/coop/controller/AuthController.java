package com.coop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import com.coop.dto.LoginDTO;
import com.coop.dto.ProfileUpdateDTO;
import com.coop.dto.SignupDTO;
import com.coop.repository.UserRepository;
import com.coop.service.UserService;

import jakarta.validation.Valid;

//로그인 라우팅
// "/auth" 하위 경로로 오는 요청을 매핑 
@Controller
@RequestMapping("/auth")
public class AuthController {
	
	private final UserService userService;

    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
	
    // 회원 가입 폼 표시
	@GetMapping("/signup")
	public String signupFrom() {
		return "signup";
	}
	
	// 회원가입 요청 처리 
	@PostMapping("/signup")
	public String signup(@Valid SignupDTO signupDTO, BindingResult result, Model model) {
	    if (result.hasErrors()) {
	        return "signup";
	    }
	    try {
	        signupDTO.validatePassword(); // 비밀번호 복잡성 검증 포함
	        userService.save(signupDTO);
	        return "redirect:/auth/login";
	    } catch (Exception e) {
	        model.addAttribute("error", e.getMessage());
	        return "signup";
	    }
	}
	
	@GetMapping("/login")
	public String loginFrom() {
		return "login";
	}
	//로그인 처리는 spring security에서 함.
	
	// 프로필 업데이트 처리
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileUpdateDTO") ProfileUpdateDTO profileUpdateDTO,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        String username = authentication.getName();
        try {
            userService.updateProfile(username, profileUpdateDTO);
            model.addAttribute("success", "프로필이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }
}
