package com.example.demo.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.config.TokenGenerator;
import com.example.demo.domain.Role;
import com.example.demo.domain.dto.LoginForm;
import com.example.demo.domain.dto.UserRegisterForm;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserRegisterer {

	private AuthenticationService authenticationService;
	
	private UserRepository userRepository;
	private TokenGenerator tokenGenerator;
    private BCryptPasswordEncoder passwordEncoder;
    
	public void registerCustomer(UserRegisterForm form) {
					
		if(List.of(Role.STYLIST, Role.ADMIN).contains(form.role()))
			throw new IllegalArgumentException("リクエストが不正です.role="+ form.role() + "は管理者のみ登録可能です");
	    User user = new User();
	    user.setEmail(form.email());
	    user.setName(form.username());
	    user.setPasswordHash(passwordEncoder.encode(form.password()));
	    user.setRole(Role.CUSTOMER);
	    
		userRepository.insert(user);
    }
	
	public String login(LoginForm form) {

		User user = authenticationService.authenticate(form);
		return tokenGenerator.generateToken(user);
	}

	
}
