package com.pairs.speed_dating.user.response;

import com.pairs.speed_dating.user.Gender;
import jakarta.validation.constraints.NotNull;

public record GetUserAgeAndGenderResponse(
  @NotNull Integer age,
  @NotNull Gender gender
) {
}
