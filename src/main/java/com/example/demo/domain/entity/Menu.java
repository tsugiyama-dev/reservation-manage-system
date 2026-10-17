package com.example.demo.domain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Menu {

	private int id;
	private String name;
	private int baseDurationMinutes;
	private int basePrice;
	
	public int getId() {return this.id;}
	public String getName() {return this.name;}
	public int getBaseDurationMinutes() {return this.baseDurationMinutes;}
	public int getBasePrice() {return this.basePrice;}
	
	public void setId(int id) {this.id = id;}
	public void setName(String name) {this.name = name;}
	public void setBaseDurationMinutes(int baseDurationMinutes) {this.baseDurationMinutes  = baseDurationMinutes;}
	public void setBasePrice(int basePrice) {this.basePrice = basePrice;}
	
	public Menu(String name, int durationMinutes, int price) {
		this.name = name;
		this.baseDurationMinutes = durationMinutes;
		this.basePrice = price;
	}
	@Override
	public String toString() {return "id=[" + id + "] name=[" + name + "] baseDurationMinutes=[" + baseDurationMinutes + "] basePrice=[" + basePrice + "]";}
	
}
