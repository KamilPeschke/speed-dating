package com.pairs.speed_dating.redis;

public record LocalizationWithRadius(
  Double lat,
  Double lon,
  Double radiusKm
) {
}
