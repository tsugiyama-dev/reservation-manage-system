package com.example.demo.config;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;

@Configuration
public class SecretKeyConfiguration {

	@Bean
	SecretKey secretKey() {
		return Keys.hmacShaKeyFor("reservationmanagementsystemsecretkey".getBytes());
	}
}
