package com.pairs.speed_dating.event;

import com.pairs.speed_dating.redis.LocalizationWithCoordinates;
import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.dto.UpdateUserStatusOutput;

public record UserChangeStatusEvent(
  UpdateUserStatusOutput output,
  LocalizationWithCoordinates localizationWithCoordinates,
  Filters filters
) {}
