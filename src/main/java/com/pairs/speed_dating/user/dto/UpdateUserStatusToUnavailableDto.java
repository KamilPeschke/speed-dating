package com.pairs.speed_dating.user.dto;

import com.pairs.speed_dating.user.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
public class UpdateUserStatusToUnavailableDto {
  @NotNull(message = "User status is required")
  private UserStatus userStatus;
}
