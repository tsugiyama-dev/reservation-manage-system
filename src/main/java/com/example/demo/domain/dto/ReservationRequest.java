package com.example.demo.domain.dto;

import java.time.LocalDateTime;

public record ReservationRequest(
		long stylistId,
		LocalDateTime date,
		long menuId/**,
		long userId**/) {

}
