package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.domain.Gender;

public record Filters(
  Integer ageFrom,
  Integer ageTo,
  Gender gender
) {}
