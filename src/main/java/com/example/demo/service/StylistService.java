package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class StylistService {

	UserRepository userRepository;
	AuthenticationService authenticationService;
	ReservationRepository reservationRepository;
	ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void confirm(long reservationId, String email) {
		
		Reservation reservation = reservationRepository.findByReservationId(new Id<Reservation>(reservationId)).orElseThrow(() -> {
			throw new NoSuchElementException("指定された予約が見つかりません");
		});
		
		try {
			authenticationService.authenticate(email, reservation.getStylistId());	
		}catch(NoSuchElementException e) {
			throw new NoSuchElementException("ユーザーデータが存在しません", e);
		}catch(AccessDeniedException e) {
			throw new AccessDeniedException("予約を操作する権限がありません", e);
		}
		
		if(!reservation.getStatus().equals(Status.PENDING)) {
			throw new IllegalStateException("保留中の予約しか操作できません");
		}
		
		reservation.setStatus(Status.CONFIRMED);
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
		
	}
	
	@Transactional
	public void reject(long reservationId, String email) {
		
		
		Reservation reservation = reservationRepository.findByReservationId(new Id<Reservation>(reservationId)).orElseThrow(() -> {
			throw new NoSuchElementException("指定された予約が見つかりません");
		});
		
		try {
			authenticationService.authenticate(email, reservation.getStylistId());	
		}catch(NoSuchElementException e) {
			throw new NoSuchElementException("ユーザーデータが存在しません", e);
		}catch(AccessDeniedException e) {
			throw new AccessDeniedException("予約を操作する権限がありません", e);
		}
		
		if(!reservation.getStatus().equals(Status.PENDING)) {
			throw new IllegalStateException("保留中の予約しか操作できません");
		}
		
		reservation.setStatus(Status.REJECTED);
		reservationRepository.update(reservation);
		
		eventPublisher.publishEvent(reservation);
		
	}

	public List<Reservation> getList(Status status) {
		reservationRepository.findByReservationId(null);
		return null;
	}

}
