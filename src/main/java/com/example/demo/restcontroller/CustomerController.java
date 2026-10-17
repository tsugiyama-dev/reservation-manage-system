package com.example.demo.restcontroller;

import static com.example.demo.service.AuthenticationService.*;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.service.ReservationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@Slf4j
@AllArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@RequestMapping("/reservations")
public class CustomerController {

	private ReservationService reservationService;
	
	@GetMapping("/me")
	public List<Reservation> getReservation(Authentication auth) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		LoginUser user = (LoginUser) auth.getPrincipal();
		return reservationService.getReservations(user);
	}
	@PostMapping
	public void reserve(
			Authentication auth,
			@RequestBody ReservationRequest<Stylist, Menu> req) {
		LoginUser user = ( LoginUser)auth.getPrincipal();
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		reservationService.reserve(req, user);
	}
	@PatchMapping("/{id}/cancel")
	public void cancel(Authentication auth, @PathVariable long id) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		LoginUser user = (LoginUser)auth.getPrincipal();
		reservationService.cancel(id, user);
	}
	@PatchMapping("/{id}")
	public void change(
			@PathVariable long id,
			Authentication  auth,
			@RequestBody ReservationRequest<Stylist, Menu> change) {
		if(!isCustomer(auth))throw new AccessDeniedException("アクセスできません");
		LoginUser user = (LoginUser)auth.getPrincipal();
		reservationService.change(user, id, change);
	}
}
