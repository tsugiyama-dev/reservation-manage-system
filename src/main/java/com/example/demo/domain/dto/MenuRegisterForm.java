package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Menu;

public record MenuRegisterForm(String name, int baseDurationMinutes, int price) {

	public static Menu toMenu(MenuRegisterForm form) {
		return new Menu(form.name(), form.baseDurationMinutes(), form.price());
	}
}
