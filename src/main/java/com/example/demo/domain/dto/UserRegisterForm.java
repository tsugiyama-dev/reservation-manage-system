package com.example.demo.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.example.demo.domain.Role;

public record UserRegisterForm (
		@NotBlank(message = "")
		String username,
		@Pattern(
		regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@?$#%&]).{8,32}$",
		message = "アルファベット,数字,記号を1字以上使用して８文字以上３２文字以下で入力してください")
		String password,
		@NotBlank(message = "^.+@.+$")
		String email,
		Role role,
		String bio
		) {
	public UserRegisterForm(String username, String password, String email, Role role) {
		this(username, password, email, role, null);
	}
}
