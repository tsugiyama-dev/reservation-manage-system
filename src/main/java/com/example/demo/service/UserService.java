package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.config.TokenGenerator;
import com.example.demo.domain.Role;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.UserForm;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.StylistRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

	private StylistRepository stylistRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private UserRepository userRepository;
	private TokenGenerator tokenGenerator;
    
    @Transactional
	public void registerUser(UserForm form) {
		
	    String encoded = passwordEncoder(form.password());
			
	    User user = new User();
	    user.setEmail(form.email());
	    user.setName(form.username());
	    user.setPassword_hash(encoded);
	    user.setRole(form.role());
	    
		userRepository.insert(user);
		
		if(form.role().equals(Role.STYLIST)) {
			stylistRepository.insert(user.getId(),
					form.bio() != null ? form.bio() : null);
		}
	}	
	
	public String findUser(String email, String password) {

		User user = findByUsername(email);	
		boolean isMatch = passwordEncoder.matches(password, user.getPassword_hash());
		
		if(!isMatch) {
			throw new IllegalArgumentException("パスワードが一致しません");
		}
		return tokenGenerator.generateToken(user.getEmail(), user.getRole().name());
	}
	public List<Stylist> findUser() {
		return stylistRepository.findAll();
	}
	@Transactional
	public void delete(long id) {
		
		stylistRepository.findById(new Id<Stylist>(id)).orElseThrow(() -> {
			throw new IllegalArgumentException("指定されたスタイリストは登録されていません: Id=" + id);
		});
		stylistRepository.delete(new Id<Stylist>(id));
		userRepository.delete(id);	
	}
	
	private User findByUsername(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("ユーザーが存在しません"));
	}
	
	private String passwordEncoder(String password) {
		return passwordEncoder.encode(password);
	}

}
