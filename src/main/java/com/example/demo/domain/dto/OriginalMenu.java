package com.example.demo.domain.dto;

import jakarta.annotation.Nullable;

public record OriginalMenu(@Nullable Integer durationMinutes,
		                  @Nullable Integer price) {
}
