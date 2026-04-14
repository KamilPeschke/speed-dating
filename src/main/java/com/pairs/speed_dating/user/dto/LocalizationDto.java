package com.pairs.speed_dating.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record LocalizationDto (
  @NotNull(message = "Latitude is required")
  @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
  @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
  Double lat,

  @NotNull(message = "Longitude is required")
  @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
  @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
  Double lon,

  @NotNull(message = "Radius is required")
  @DecimalMin(value = "0.0", message = "Radius must be at least 0 km")
  @DecimalMax(value = "3", message = "Radius must be at most 3 km")
  Double radiusKm
  ){}
