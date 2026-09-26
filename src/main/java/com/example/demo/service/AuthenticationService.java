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

@Component
@AllArgsConstructor
public class AuthenticationService {

	public UserRepository userRepository;
	
	public boolean authenticate(String email, long uid) throws NoSuchElementException, AccessDeniedException{
		User user = userRepository.findById(new Id<User>(uid)).orElseGet(() -> {
			throw new NoSuchElementException("認証できませんでした");
		});
		
		if(!email.equals(user.getEmail())) {
			throw new AccessDeniedException("認証できませんでした");
		}
		
		return true;
	}
	
	public static boolean isAdmin(Authentication auth) {
		return auth.getPrincipal() != null 
				&& ((String)auth.getPrincipal()).equals(Role.ADMIN.name());
	}
	public static boolean isStylist(Authentication auth) {
		return auth.getPrincipal() != null 
				&& ((String)auth.getPrincipal()).equals(Role.STYLIST.name());
	}
}
