package com.pairs.speed_dating.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusToAvailableDto (
  @NotNull(message = "Localization is required")
  @Valid
  LocalizationDto localization,

  @NotNull(message = "Filters like userAge, userGender, etc are required")
  @Valid
  FiltersDto filters
){}
