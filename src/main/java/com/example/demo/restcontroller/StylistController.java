package com.example.demo.restcontroller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.service.StylistService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/reservations")
public class StylistController {

	StylistService stylistService;
	
	@GetMapping("/me/status")
	public List<Reservation> reservations(
			@RequestParam Status status,
			Authentication auth) {
		
		LoginUser user = (LoginUser)auth.getPrincipal();
		return stylistService.getReservations(status, user);
	}
	
	@PatchMapping("/{id}/confirm")
	public void reservationConfirm(
			@PathVariable long id,
			Authentication auth) {
		LoginUser user = (LoginUser)auth.getPrincipal();
		stylistService.confirm(id, user);
	}
	
	@PatchMapping("/{id}/reject")
	public void reservationReject(
			@PathVariable long id,
			Authentication auth) {
		LoginUser user = (LoginUser)auth.getPrincipal();
		stylistService.reject(id, user);
	}
}
