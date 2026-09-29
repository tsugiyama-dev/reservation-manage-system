package com.example.demo.domain.entity.impl;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

public final class Pending implements ReservationState {

	private Reservation reservation;
	
	@Override
	public void cancel() {
		reservation.setStatus(Status.REJECTED);
	}
	@Override
	public Status getKey() {
		return Status.PENDING;
	}
	@Override
	public void set(Reservation reservation) {
		this.reservation = reservation;
		
	}

}
