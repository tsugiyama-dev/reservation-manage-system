package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@Component
public final class Cancel implements ReservationState {
	private Reservation reservation;
	
	public Status getKey() {
		return Status.CANCELLED;
	}
	public void set(Reservation reservation) {
		this.reservation = reservation;
	}

}
