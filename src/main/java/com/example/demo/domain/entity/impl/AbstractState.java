package com.example.demo.domain.entity.impl;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

public abstract class AbstractState {

	protected Reservation reservation;
	
	abstract Status getKey();
	public void set(Reservation reservation) {
		this.reservation = reservation;
	}
	
}
