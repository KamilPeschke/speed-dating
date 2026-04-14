package com.pairs.speed_dating.user.dto;

import jakarta.validation.constraints.*;
import com.pairs.speed_dating.user.Gender;

public record CreateUserDto(
  @NotBlank(message = "Email should not be empty")
  @Email(message = "Wrong email format")
  String email,

  @NotBlank(message = "Password is required")
  @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")
  String password,

  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
  String name,

  @NotBlank(message = "Surname is required")
  String surname,

  @NotNull(message = "Age is required")
  @Min(value = 18, message = "You must be at least 18 years old")
  @Max(value = 100, message = "Age must be less than 100")
  Integer age,

  @NotNull(message = "Gender is required")
  Gender gender,

  @NotNull(message = "Interested in is required")
  Gender interestedIn
) {}
