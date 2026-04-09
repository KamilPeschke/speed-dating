package com.pairs.speed_dating.user.dto;

import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusToAvailableDto {
  @NotNull(message = "User status is required")
  UserStatus status;

  @NotNull(message = "Localization is required")
  @Valid
  private LocalizationDto localization;

  @NotNull(message = "Filters like age, gender, etc are required")
  @Valid
  private Filters filters;
}
