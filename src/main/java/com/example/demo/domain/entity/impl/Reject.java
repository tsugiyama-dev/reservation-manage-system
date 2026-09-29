package com.example.demo.domain.entity.impl;


import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@Component
public final class Reject implements ReservationState {

		
	public Status getKey() {
		return Status.REJECTED;
	}

	@Override
	public void set(Reservation reservation) {
		return;
	}
	
}
