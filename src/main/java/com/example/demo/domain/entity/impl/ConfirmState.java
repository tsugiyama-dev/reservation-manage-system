package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@Component
public final class ConfirmState implements ReservationState {

	
	@Override
	public Status getKey() {
		return Status.CONFIRMED;
	}
	@Override
	public void pending(Reservation reservation) {
		reservation.setStatus(Status.PENDING);
	}
	@Override
	public void cancel(Reservation reservation) {
		reservation.setStatus(Status.CANCELLED);
	}
}
