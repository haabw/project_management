package com.coop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import com.coop.dto.LoginDTO;
import com.coop.dto.SignupDTO;
import com.coop.repository.UserRepository;
import com.coop.service.UserService;

//로그인 라우팅
// "/auth" 하위 경로로 오는 요청을 매핑 
@Controller
@RequestMapping("/auth")
public class AuthController {
	
	private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
	
    // 회원 가입 폼 표시
	@GetMapping("/signup")
	public String signupFrom() {
		return "signup";
	}
	
	// 회원가입 요청 처리 
	@PostMapping("/signup")
	public String signup(SignupDTO signupDTO, Model model) {
		try {
			// 사용자 정보 저장 
			userService.save(signupDTO);
			return "redirect:/auth/login";
		} catch (Exception e) {
			// 에러 발생 시 에러 메시지를 모델에 추가하고 회원 가입 폼으로 반환 
			model.addAttribute("error", e.getMessage());
			return "signup";
		}
	}
	
	@GetMapping("/login")
	public String loginFrom() {
		return "login";
	}
	//로그인 처리는 spring security에서 함.
}
