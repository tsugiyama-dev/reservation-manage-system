package com.example.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stylist {

	private long userId; 
	private String name;
	private String email;
	private String bio; // 自己紹介文
	
	// Getter
	public long getUserId() {return this.userId;}
	public String getName() {return this.name;}
	public String getEmail() {return this.email;}
	public String getBio() {return this.bio;}
	
	// Setter
	public void setUserId(long userId) {this.userId = userId;}
	public void setName(String name) {this.name = name;}
	public void setEmail(String email) {this.email = email;}
	public void setBio(String bio) {this.bio = bio;}
}
