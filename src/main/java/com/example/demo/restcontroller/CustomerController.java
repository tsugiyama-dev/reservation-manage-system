package com.example.demo.restcontroller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.TentativeReservationRequest;
import com.example.demo.service.CustomerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/reservations")
public class CustomerController {

	CustomerService customerService;
	
	@PostMapping
	public void tempReservation(
			@RequestBody TentativeReservationRequest req) {
		customerService.tentativeReserve(req.stylistId(), req.date(), req.menuId(), req.userId());
	}
	@GetMapping("/me")
	public void list() {
		
	}
	@PatchMapping("/{id}/cancel")
	public void cancel(Authentication auth, @PathVariable long id) {
		String email = (String)auth.getPrincipal();
		customerService.cancel(id, email);
	}
	@PatchMapping("/{id}")
	public void changeDate() {
		
	}
}
