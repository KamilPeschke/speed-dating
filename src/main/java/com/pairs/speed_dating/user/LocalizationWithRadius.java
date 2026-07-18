package com.pairs.speed_dating.user;

public record LocalizationWithRadius(
  double lat,
  double lon,
  double radiusKm
) {
}
