package com.pairs.speed_dating.user.dto.response;

import com.pairs.speed_dating.user.domain.Gender;
import jakarta.validation.constraints.NotNull;

public record GetUserAgeAndGenderResponse(
  @NotNull Integer age,
  @NotNull Gender gender
) {
}
