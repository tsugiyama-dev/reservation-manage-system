package com.example.demo.domain.dto;

import java.time.LocalDateTime;

public record TentativeReservationRequest(long stylistId, LocalDateTime date, long menuId, long userId) {

}
