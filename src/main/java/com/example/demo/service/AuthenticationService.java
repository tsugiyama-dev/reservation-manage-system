package com.example.demo.service;

import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Role;
import com.example.demo.domain.dto.LoginForm;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class AuthenticationService {

	public UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	
	public  User authenticate(LoginForm form) {
		User user = userRepository.findByEmail(form.email()).orElseGet(() -> {
			log.info("ユーザーが見つかりません : email = {}", form.email());
			throw new NoSuchElementException("ユーザーが見つかりません");
		});
		if(!passwordEncoder.matches(form.password(), user.getPasswordHash())) {
			log.info("パスワードが一致しません : email = {}", form.email());
			throw new AccessDeniedException("パスワードが一致しません");
		};
		return user;
	}
	
	public void identify(LoginUser user, long id) {
		if(user.id() != id) {
			log.info("本人以外操作することはできません : userId = {}, id = {}", user.id(), id);
			throw new AccessDeniedException("本人以外操作することはできません");
		}
	}
	
	public static boolean isAdmin(Authentication auth) {
		return auth.getPrincipal() != null 
				&& auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList().contains("ROLE_" + Role.ADMIN.name());
	}
	public static boolean isStylist(Authentication auth) {
		return auth.getPrincipal() != null 
				&& auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList().contains("ROLE_" + Role.STYLIST.name());
	}
	public static boolean isCustomer(Authentication auth) {	
		return auth.getPrincipal() != null 
				&& auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList().contains("ROLE_" + Role.CUSTOMER.name());
	}
}
