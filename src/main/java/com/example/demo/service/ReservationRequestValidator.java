package com.example.demo.service;

import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.demo.ReservationStatusResolver;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReservationRequestValidator {

	private ReservationStatusResolver resolver;
	private ReservationRepository reservationRepository;
	private UserRepository userRepository;
	private AuthenticationService authenticationService;
	
	public Reservation cancelCheck(long rid, String email) {
		Reservation reservation = reservationRepository.findById(
				new Id<Reservation>(rid)).orElseThrow(() -> {
			throw new NoSuchElementException("指定された予約が見つかりません");
		});
		long cid = reservation.getCustomerId();
		authenticationService.authenticate(email, cid);	
		
		var status = resolver.getInstance(reservation.getStatus());
		status.set(reservation);
		status.cancel();
		
		return reservation;
	}

	public long changeCheck(String email, long rid) {
		
		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			throw new IllegalArgumentException("ユーザーが見つかりません");
		});
		Reservation reservation = reservationRepository.findById(new Id<Reservation>(rid)).orElseThrow(() -> {
			throw new NoSuchElementException("指定された予約が見つかりません");
		});
		if(reservation.getCustomerId() != user.getId()) {
			throw new AccessDeniedException("本人以外操作することはできません");
		}
		if(!reservation.getStatus().equals(Status.PENDING)) {
			throw new IllegalStateException("""
					保留中以外は変更できません"
					予約が確定している場合はキャンセルをしてから再度予約してください
					""");
		}
		return user.getId();
	}

}
