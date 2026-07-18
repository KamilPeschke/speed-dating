package com.pairs.speed_dating.redis;

public record LocalizationWithRadius(
  double lat,
  double lon,
  double radiusKm
) {
}
