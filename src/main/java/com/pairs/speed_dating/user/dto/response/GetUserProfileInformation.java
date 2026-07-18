package com.pairs.speed_dating.user.dto.response;

import com.pairs.speed_dating.user.domain.Gender;

public record GetUserProfileInformation(
  Integer age,
  Gender gender,
  Gender interestedIn,
  String name
) {
}
