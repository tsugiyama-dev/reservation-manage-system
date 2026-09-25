package com.example.demo.domain.dto;

import jakarta.annotation.Nullable;

import com.example.demo.domain.Role;

public record UserForm(
		String username,
		String password,
		String email,
		Role role,
		@Nullable String bio
		) {
	public UserForm(String username, String password, String email, Role role) {
		this(username, password, email, role, null);
	}
}
