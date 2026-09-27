package com.example.demo.service;

import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Role;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class AuthenticationService {

	public UserRepository userRepository;
	
	public void authenticate(String email, long uid) throws NoSuchElementException, AccessDeniedException{
		User user = userRepository.findById(new Id<User>(uid)).orElseGet(() -> {
			log.info("ユーザーが見つかりません:[id = {}]", uid);
			throw new NoSuchElementException("ユーザーが見つかりません: [id = " + uid + "]");
		});
		if(!email.equals(user.getEmail())) {
			log.info("本人以外認証できません:[email = {}]", user.getEmail());
			throw new AccessDeniedException("本人ではないため認証できませんでした");
		}
	}
	
	
	public static boolean isAdmin(Authentication auth) {
		return auth.getPrincipal() != null 
				&& auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList().contains("ROLE_" + Role.ADMIN.name());
	}
	public static boolean isStylist(Authentication auth) {
		log.info("権限一覧={}", auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList());
		
		return auth.getPrincipal() != null 
				&& auth.getAuthorities().stream().map(authority -> 
				authority.getAuthority()).toList().contains("ROLE_" + Role.STYLIST.name());
	}
}
