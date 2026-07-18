package com.pairs.speed_dating.user;

import com.pairs.speed_dating.core.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserChangeStatusToAvailableEvent(
  UUID eventId,
  Instant occurredOn,
  UserChangeStatusTo output,
  LocalizationWithRadius localization,
  Filters filters
) implements DomainEvent {

  public UserChangeStatusToAvailableEvent(UserChangeStatusTo output, LocalizationWithRadius localization, Filters filters) {
    this(UUID.randomUUID(), Instant.now(), output, localization, filters);
  }
}
