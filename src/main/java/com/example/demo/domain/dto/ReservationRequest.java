package com.example.demo.domain.dto;

import java.time.LocalDateTime;

public record ReservationRequest<S, M>(
		Id<S> stylistId,
		LocalDateTime date,
		Id<M> menuId) {

}
