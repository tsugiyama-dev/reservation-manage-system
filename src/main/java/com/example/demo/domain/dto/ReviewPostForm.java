package com.example.demo.domain.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewPostForm(@Min(1) @Max(5)Integer rating, @Nullable String comment) {

}
