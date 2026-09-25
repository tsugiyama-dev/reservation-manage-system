package com.example.demo.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.NotFoundUserException;
import com.example.demo.config.TokenGenerator;
import com.example.demo.domain.Role;
import com.example.demo.domain.dto.UserForm;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.StylistRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

	StylistRepository stylistRepository;
	BCryptPasswordEncoder passwordEncoder;
	UserRepository userRepository;
    TokenGenerator tokenGenerator;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
	public void registerUser(UserForm form) {
		
	    String encoded = passwordEncoder(form.password());
			
	    User user = new User();
	    user.setEmail(form.email());
	    user.setName(form.username());
	    user.setPassword_hash(encoded);
	    user.setRole(form.role());
	    
		userRepository.insert(user);
		
		if(form.role().equals(Role.STYLIST)) {
			stylistRepository.insert(user.getId(), encoded);
		}
	}	
	
	public String findUser(String username, String password) {
		User user = findByUsername(username);
		if(user == null) {throw new NotFoundUserException("ユーザーが見つかりません");}
		
		boolean isMatch = passwordEncoder.matches(password, user.getPassword_hash());
		
		if(!isMatch) {
			throw new IllegalArgumentException("パスワードが一致しません");
		}
		
		return tokenGenerator.generateToken(user.getEmail(), user.getRole().name());
		
	}
	public List<User> findUser(String role) {
		
		return userRepository.findByRole(role);
	}
	
	private User findByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません"));
	}
	
	private String passwordEncoder(String password) {
		return passwordEncoder.encode(password);
	}
}
