package com.example.demo.domain.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.example.demo.domain.Role;

public record UserRegisterForm (
		@NotBlank(message = "")
		String username,
		@Pattern(
		regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@?$#%&]){8,32}$",
		message = "英数字記号をすべて1文字以上入れて８文字以上３２文字以下で入力してください")
		String password,
		@NotBlank(message = "^.+@.+$")
		String email,
		@Nullable
		Role role,
		@Nullable
		String bio
		) {
	public UserRegisterForm(String username, String password, String email, Role role) {
		this(username, password, email, role, null);
	}
}
