package com.example.demo.domain.entity.impl;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;

@Component
public final class CompleteState implements ReservationState {

	@Override
	public Status getKey() {
		return Status.COMPLETED;
	}

}
