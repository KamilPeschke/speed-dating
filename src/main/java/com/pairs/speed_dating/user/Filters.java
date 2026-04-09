package com.pairs.speed_dating.user;

public record Filters(
  Integer ageFrom,
  Integer ageTo,
  Gender gender
) {
}
