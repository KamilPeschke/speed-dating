package com.pairs.speed_dating.user.event;

import com.pairs.speed_dating.user.api.Gender;

public record SearchPreferences(
  Integer ageFrom,
  Integer ageTo,
  Gender gender
) {
}
