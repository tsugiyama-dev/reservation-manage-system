package com.example.demo.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;

@Configuration
public class SecretKeyConfiguration {

	
	@Bean
	SecretKey key(@Value("${jwt.secretKey}")String secretKey) {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}
}
