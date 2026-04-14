package com.pairs.speed_dating.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginDto (
  @Email(message = "Invalid email format")
  @NotNull(message = "Email is required")
  String email,

  @NotNull(message = "Password is required")
  String password
){}
