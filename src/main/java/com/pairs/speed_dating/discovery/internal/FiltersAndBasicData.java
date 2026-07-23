package com.pairs.speed_dating.discovery.internal;

import com.pairs.speed_dating.user.api.Gender;

public record FiltersAndBasicData(
  Filters filters,
  int currentUserAge,
  Gender currentUserGender
) {
}
