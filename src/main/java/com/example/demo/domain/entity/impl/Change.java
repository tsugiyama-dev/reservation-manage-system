package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@Component
public final class Change implements ReservationState {
	private Reservation reservation;
	@Override
	public void pending() {
		reservation.setStatus(Status.PENDING);
	}
	public Status getKey() {
		return Status.CHANGE;
	}
	public void set(Reservation reservation) {
		this.reservation = reservation;
	}

}
