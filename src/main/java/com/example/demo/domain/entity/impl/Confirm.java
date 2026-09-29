package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;

@Component
public final class Confirm extends AbstractState implements ReservationState {

	
	@Override
	public Status getKey() {
		return Status.CONFIRMED;
	}
	@Override
	public void cancel() {
		reservation.setStatus(Status.CANCELLED);
	}
}
