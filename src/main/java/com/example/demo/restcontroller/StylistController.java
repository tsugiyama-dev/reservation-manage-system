package com.example.demo.restcontroller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Status;
import com.example.demo.service.StylistService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/reservations")
public class StylistController {

	StylistService stylistService;
	
	@GetMapping
	public void reservationView(
			@RequestParam Status status) {
	}
	
	@PatchMapping("/{id}/confirm")
	public void reservationConfirm(
			@PathVariable long id,
			Authentication auth) {
		String email = (String)auth.getPrincipal();
		stylistService.confirm(id, email);
	}
	
	@PatchMapping("/{id}/reject")
	public void reservationReject(
			@PathVariable long id,
			Authentication auth) {
		String email = (String)auth.getPrincipal();
		stylistService.reject(id, email);
	}
}
