package com.example.demo.service;

public class SqlErrorException extends RuntimeException {

	public SqlErrorException(String message, Throwable t) {
		super(message, t);
	}
}
