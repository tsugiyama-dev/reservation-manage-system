package com.example.demo.domain.entity.impl;


import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;

@Component
public final class RejectState implements ReservationState {
		
	public Status getKey() {
		return Status.REJECTED;
	}
	
}
