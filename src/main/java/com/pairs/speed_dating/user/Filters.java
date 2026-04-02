package com.pairs.speed_dating.user;

public record Filters(
  int ageFrom,
  int ageTo,
  Gender gender
) {
}
