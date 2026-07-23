package com.pairs.speed_dating.user.dto.response;

import com.pairs.speed_dating.user.api.Gender;

public record GetUserProfileInformation(
  Integer age,
  Gender gender,
  Gender interestedIn,
  String name
) {
}
