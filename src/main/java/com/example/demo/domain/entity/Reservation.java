package com.example.demo.domain.entity;

import java.time.LocalDateTime;

import com.example.demo.domain.Status;

public class Reservation {

	private long id;
	private long customerId;
	private long stylistId;
	private long menuId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Status status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public long getId() {return this.id;}
	public long getCustomerId() {return this.customerId;}
	public long getStylistId() {return this.stylistId;}
	public long getMenuId() {return this.menuId;}
	public LocalDateTime getStartTime() {return this.startTime;}
	public LocalDateTime getEndTime() {return this.endTime;}
	
	public Status getStatus() {return this.status;}
	public LocalDateTime getCreatedAt() {return this.createdAt;}
	public LocalDateTime getUpdatedAt() {return this.updatedAt;}

	public void setId(long id) {this.id = id;}
	public void setCustomerId(long customerId) {this.customerId = customerId;}
	public void setStylistId(long stylistId) {this.stylistId = stylistId;}
	public void setMenuId(long menuId) {this.menuId = menuId;}
	public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}
	public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}
	public void setStatus(Status status) {this.status = status;}
	public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
	public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
