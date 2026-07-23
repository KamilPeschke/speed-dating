package com.pairs.speed_dating.user.event;

public record SearchArea(
  double lat,
  double lon,
  double radiusKm
) {
}
