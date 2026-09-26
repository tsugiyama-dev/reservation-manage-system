package com.example.demo.restcontroller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.LoginForm;
import com.example.demo.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

	UserService userService;
	
	@PostMapping("/login")
	public String login(@RequestBody LoginForm form) {
	
		String token = userService.findUser(form.username(), form.password());	
		return token;
	}
	
	
}
