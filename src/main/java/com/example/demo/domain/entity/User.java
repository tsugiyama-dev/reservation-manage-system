package com.example.demo.domain.entity;

import com.example.demo.domain.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

	private long id;
	private String name;
	private String email;
	private String passwordHash;
	private Role role;
	private String createdAt;
	private Stylist stylist;
	
	// getters and setters
	
	public long getId() {return this.id;}
	public String getName() {return this.name;}
	public String getEmail() {return this.email;}
	public String getPasswordHash() {return this.passwordHash;}
	public Role getRole() {return this.role;}
	public String getCreatedAt() {return this.createdAt;}
	public Stylist getStylist() {return this.stylist;}
	
	public void setId(long id) {this.id = id;}
	public void setName(String name) {this.name = name;}
	public void setEmail(String email) {this.email = email;}
	public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
	public void setRole(Role role) {this.role = role;}
	public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}
	public void setStylist(Stylist stylist) {this.stylist = stylist;}
}
	
