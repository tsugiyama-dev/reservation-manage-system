package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.ReservationStatusResolver;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.impl.ReservationState;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class StylistService {

	private final ReservationStatusResolver resolver;
	private final UserRepository userRepository;
	private final AuthenticationService authenticationService;
	private final ReservationRepository reservationRepository;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void confirm(long reservationId, String email) {
		
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));

		authenticationService.authenticate(email, reservation.getStylistId());	

		ReservationState state = resolver.getInstance(reservation.getStatus());
		state.set(reservation);
		state.confirm();
		
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
	}
	
	@Transactional
	public void reject(long reservationId, String email) {
		
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));

		authenticationService.authenticate(email, reservation.getStylistId());	

		var status = resolver.getInstance(reservation.getStatus());
		status.set(reservation);
		status.reject();
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
		
	}

	public List<Reservation> getList(Status status, String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			throw new IllegalArgumentException("見つかりません:[email=" + email + "]");
		});
		return reservationRepository.findByStatus(new Id<User>(user.getId()), status);
		
	}
	private Reservation getReservation(Id<Reservation> rid) {
		Optional<Reservation> reservation = reservationRepository.findById(rid);
		if(reservation.isEmpty()) {
			throw new NoSuchElementException("指定された予約が見つかりません: 予約ID=" + rid);
		}
		return reservation.get(); 
	}

}
