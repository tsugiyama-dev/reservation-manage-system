package com.example.demo.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BusinessExceptionHandler {

	
	
	@ExceptionHandler
	public ResponseEntity<Map<String, String>> accessDeniedException(AccessDeniedException e) {
		Map<String, String> message = new HashMap<>();
		message.put("error", e.getMessage());
		
		return ResponseEntity.status(403).body(message);
	}
	@ExceptionHandler
	public ResponseEntity<Map<String, String>> noSuchElementException(NoSuchElementException e) {
		Map<String, String> message = new HashMap<>();
		message.put("error", e.getMessage());
		
		return ResponseEntity.status(404).body(message);
	}
	@ExceptionHandler
	public ResponseEntity<Map<String, String>> concurrentModifyException(IllegalStateException e) {
		Map<String, String> message = new HashMap<>();
		message.put("error", e.getMessage());
		return ResponseEntity.status(409).body(message);
	}
}
