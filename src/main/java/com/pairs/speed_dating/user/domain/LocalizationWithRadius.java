package com.pairs.speed_dating.user.domain;

public record LocalizationWithRadius(
  double lat,
  double lon,
  double radiusKm
) {
}
