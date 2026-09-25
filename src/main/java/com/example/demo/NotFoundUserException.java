package com.example.demo;

public class NotFoundUserException extends RuntimeException {

	private String message;
	
	public NotFoundUserException(String message) {
		super(message);
		this.message = message;
	}
	public String getMessage() {return this.message;}
}
