package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@Component
public final class PendingState implements ReservationState {
	
	@Override
	public void cancel(Reservation reservation) {
		reservation.setStatus(Status.CANCELLED);
	}
	@Override
	public void confirm(Reservation reservation) {
		reservation.setStatus(Status.CONFIRMED);
	}
	@Override
	public void reject(Reservation reservation) {
		reservation.setStatus(Status.REJECTED);
	}
	@Override
	public void pending(Reservation reservation) {
		reservation.setStatus(Status.PENDING);
	}
	@Override
	public Status getKey() {
		return Status.PENDING;
	}


}
