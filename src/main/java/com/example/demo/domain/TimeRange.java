package com.example.demo.domain;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

@Getter
public class TimeRange {

	private LocalTime startTime;
	private LocalTime endTime;
	private long durationTime;
	
	public TimeRange(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.durationTime = ChronoUnit.HOURS.between(startTime, endTime);
	}
}
