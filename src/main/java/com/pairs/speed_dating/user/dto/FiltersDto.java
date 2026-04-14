package com.pairs.speed_dating.user.dto;

import com.pairs.speed_dating.user.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FiltersDto(
  @NotNull(message = "Min userAge is required")
  @Min(value = 18, message = "Age must be at least 18")
  @Max(value = 80, message = "Age must be at most 80")
  Integer ageFrom,

  @NotNull(message = "Max userAge to is required")
  @Min(value = 18, message = "Age must be at least 18")
  @Max(value = 80, message = "Age must be at most 80")
  Integer ageTo,

  @NotNull(message = "Gender is required")
  Gender gender
) {
}
