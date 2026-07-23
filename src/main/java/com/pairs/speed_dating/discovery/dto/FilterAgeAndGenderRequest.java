package com.pairs.speed_dating.discovery.dto;

import com.pairs.speed_dating.discovery.internal.Filters;
import com.pairs.speed_dating.discovery.internal.LocalizationWithRadius;
import com.pairs.speed_dating.user.api.Gender;

//TODO change that to DTO
public record FilterAgeAndGenderRequest(
  Integer userAge,
  Gender userGender,
  LocalizationWithRadius localization,
  Filters filters
) {
}
