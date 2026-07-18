package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.LocalizationWithRadius;
import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.domain.Gender;

//TODO change that to DTO
public record FilterAgeAndGenderRequest(
  Integer userAge,
  Gender userGender,
  LocalizationWithRadius localization,
  Filters filters
) {
}
