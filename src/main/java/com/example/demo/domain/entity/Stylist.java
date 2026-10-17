package com.example.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stylist {

	private long userId; 
	private String bio; // 自己紹介文

	
	// Getter
	public long getUserId() {return this.userId;}
	public String getBio() {return this.bio;}

	// Setter
	public void setUserId(long userId) {this.userId = userId;}
	public void setBio(String bio) {this.bio = bio;}
	
}
