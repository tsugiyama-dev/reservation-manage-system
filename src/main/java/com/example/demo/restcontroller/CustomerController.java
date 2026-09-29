package com.example.demo.restcontroller;

import static com.example.demo.service.AuthenticationService.*;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.service.CustomerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/reservations")
public class CustomerController {

	private CustomerService customerService;
	
	@PostMapping
	public void reservation(
			Authentication auth,
			@RequestBody ReservationRequest req) {
		String email = (String)auth.getPrincipal();
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		customerService.reserve(req,email);
	}
	@GetMapping("/me")
	public List<Reservation> list(Authentication auth) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		String email = (String) auth.getPrincipal();
		return customerService.getReservationList(email);
	}
	@PatchMapping("/{id}/cancel")
	public void cancel(Authentication auth, @PathVariable long id) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		String email = (String)auth.getPrincipal();
		customerService.cancel(id, email);
	}
	@PatchMapping("/{id}")
	public void changeDate(
			@PathVariable long id,
			Authentication auth,
			@RequestBody ReservationRequest change) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		String email = (String)auth.getPrincipal();
		customerService.changeSchedule(email, id, change);
	}
}
