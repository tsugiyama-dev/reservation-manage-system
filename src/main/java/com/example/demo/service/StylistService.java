package com.example.demo.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.impl.ReservationStatusResolver;
import com.example.demo.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StylistService {

	private final ReservationStatusResolver resolver;
	private final AuthenticationService authenticationService;
	private final ReservationRepository reservationRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;
	
	@Transactional
	public void confirm(long reservationId, LoginUser user) {
		
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));
		authenticationService.identify(user, reservation.getStylistId());	
		resolver.getInstance(reservation.getStatus()).confirm(reservation);
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
	}
	
	@Transactional
	public void reject(long reservationId, LoginUser user) {
		
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));
		authenticationService.identify(user, reservation.getStylistId());	
		resolver.getInstance(reservation.getStatus()).reject(reservation);
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
	}

	public List<Reservation> getReservations(Status status, LoginUser user) {
		
		return reservationRepository.findByStatus(new Id<User>(user.id()), status);
		
	}
	@Transactional
	public void complete(long reservationId, LoginUser user) {
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));
		authenticationService.identify(user, reservation.getStylistId());
		
		if(LocalDateTime.now(clock).isBefore(reservation.getEndTime())) {
			throw new IllegalStateException("予約時間より前にステータスを変更できません");
		}
		resolver.getInstance(reservation.getStatus()).complete(reservation);
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
	}
	private Reservation getReservation(Id<Reservation> rid) {
		Optional<Reservation> reservation = reservationRepository.findById(rid);
		if(reservation.isEmpty()) {
			throw new NoSuchElementException("指定された予約が見つかりません: 予約ID=" + rid);
		}
		return reservation.get(); 
	}

}
