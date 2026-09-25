package com.example.demo.domain.dto;

public record StylistMenu(long stylistId,
		                  long menuId,
		                  int duration_minutes,
		                  int price) {
	public StylistMenu(long stylistId, long menuId) {
		this(stylistId, menuId, 0, 0);
	}

}
