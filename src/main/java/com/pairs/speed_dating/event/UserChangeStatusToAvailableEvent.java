package com.pairs.speed_dating.event;

import com.pairs.speed_dating.user.domain.LocalizationWithRadius;
import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.response.UpdateUserStatusResponse;

import java.time.Instant;
import java.util.UUID;

public record UserChangeStatusToAvailableEvent(
  UUID eventId,
  Instant occurredOn,
  UpdateUserStatusResponse output,
  LocalizationWithRadius localization,
  Filters filters
) implements DomainEvent {

  public UserChangeStatusToAvailableEvent(UpdateUserStatusResponse output, LocalizationWithRadius localization, Filters filters) {
    this(UUID.randomUUID(), Instant.now(), output, localization, filters);
  }
}
