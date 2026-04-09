package com.pairs.speed_dating.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
  @Email(message = "Invalid email format")
  @NotNull(message = "Email is required")
  private String email;

  @NotNull(message = "Password is required")
  private String password;
}
