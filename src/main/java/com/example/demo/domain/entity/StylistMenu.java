package com.example.demo.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StylistMenu {

	private long id;
	private long stylistId;
	private long menuId;
	private long durationMinutes;
	private long price;
	
	public StylistMenu(long stylistId, long menuId, long durationMinutes, long price) {
		this.stylistId = stylistId;
		this.menuId = menuId;
		this.durationMinutes = durationMinutes;
		this.price = price;
	}
}
