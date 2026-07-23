package com.pairs.speed_dating.discovery.internal;

import com.pairs.speed_dating.user.api.Gender;

public record Filters(
  Integer ageFrom,
  Integer ageTo,
  Gender gender
) {}
