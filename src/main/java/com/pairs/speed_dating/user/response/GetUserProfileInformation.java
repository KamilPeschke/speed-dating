package com.pairs.speed_dating.user.response;

import com.pairs.speed_dating.user.Gender;

public record GetUserProfileInformation(
  Integer age,
  Gender gender,
  Gender interestedIn,
  String name
) {
}
