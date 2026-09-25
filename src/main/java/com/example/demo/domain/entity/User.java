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
	private String password_hash;
	private Role role;
	private String created_at;
	
	// getters and setters
	
	public long getId() {return this.id;}
	public String getName() {return this.name;}
	public String getEmail() {return this.email;}
	public String getPassword_hash() {return this.password_hash;}
	public Role getRole() {return this.role;}
	public String getCreatedAt() {return this.created_at;}
	
	public void setId(long id) {this.id = id;}
	public void setName(String name) {this.name = name;}
	public void setEmail(String email) {this.email = email;}
	public void setPassword_hash(String password_hash) {this.password_hash = password_hash;}
	public void setRole(Role role) {this.role = role;}
	public void setCreatedAt(String created_at) {this.created_at = created_at;}
}
	
