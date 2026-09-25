package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.BusinessHours;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

	@GetMapping
	public String helthCheck() {
		
		log.info("ヘルスチェックが呼び出されました");
		return "OK";
	
	}
	
	@GetMapping("/body")
	public String json(@RequestBody BusinessHours body) {
		log.info("json={}", body);
		
		return "";
	}
}
