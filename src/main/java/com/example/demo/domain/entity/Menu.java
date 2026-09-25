package com.example.demo.domain.entity;

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
	
	@Override
	public String toString() {return "id=[" + id + "] name=[" + name + "] baseDurationMinutes=[" + baseDurationMinutes + "] basePrice=[" + basePrice + "]";}
	
}
