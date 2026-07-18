package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.Gender;

public record FiltersAndBasicData(
  Filters filters,
  int currentUserAge,
  Gender currentUserGender
) {
}
