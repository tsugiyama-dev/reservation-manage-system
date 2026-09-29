package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class 予約管理システムApplication {

	private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	public static void main(String[] args) {
		SpringApplication.run(予約管理システムApplication.class, args);
		System.out.println("予約管理システムが起動しました。" + encoder.encode("P@ssw0rd"));
	}

}
