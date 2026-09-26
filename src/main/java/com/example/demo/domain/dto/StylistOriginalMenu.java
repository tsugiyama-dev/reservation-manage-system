package com.example.demo.domain.dto;

import jakarta.annotation.Nullable;

public record StylistOriginalMenu(
		                  @Nullable Integer duration_minutes,
		                  @Nullable Integer price) {
}
