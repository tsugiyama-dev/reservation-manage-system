package com.example.demo.restcontroller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.UserRegisterForm;
import com.example.demo.service.UserRegisterer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/register")
public class UserRegisterController {

	UserRegisterer userService;
	
	@PostMapping
	public void register(@RequestBody @Valid UserRegisterForm form) {
		userService.registerCustomer(form);
		
	}
}
