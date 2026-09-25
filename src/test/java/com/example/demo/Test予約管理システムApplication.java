package com.example.demo;

import org.springframework.boot.SpringApplication;

public class Test予約管理システムApplication {

	public static void main(String[] args) {
		SpringApplication.from(予約管理システムApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
