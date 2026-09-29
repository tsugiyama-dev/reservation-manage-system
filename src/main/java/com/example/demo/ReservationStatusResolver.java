package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.impl.ReservationState;

@Component
public class ReservationStatusResolver {

	private final Map<Status, ReservationState> status = new HashMap<>();
	
	public ReservationStatusResolver(@Autowired List<ReservationState> states) {
		for(var state : states) {
			status.put(state.getKey(), state);
		}
	}
	
	public ReservationState getInstance(Status key) {
		return status.get(key);
	}
	
}
