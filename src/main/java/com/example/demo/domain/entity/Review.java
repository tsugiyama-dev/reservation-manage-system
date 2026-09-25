package com.example.demo.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Review {

	private long id;
	private String comment;
	private long reviewCount;
	private double reviewRating;
	private LocalDateTime createdAt;
}
