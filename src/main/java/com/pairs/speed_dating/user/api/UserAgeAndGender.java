package com.pairs.speed_dating.user.api;

import jakarta.validation.constraints.NotNull;

public record UserAgeAndGender(
  @NotNull Integer age,
  @NotNull Gender gender
) {
}
