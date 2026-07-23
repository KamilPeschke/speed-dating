package com.pairs.speed_dating.user.event;

import com.pairs.speed_dating.core.event.DomainEvent;
import com.pairs.speed_dating.user.api.UserChangeStatusTo;

import java.time.Instant;
import java.util.UUID;

public record UserChangeStatusToAvailableEvent(
  UUID eventId,
  Instant occurredOn,
  UserChangeStatusTo output,
  SearchArea searchArea,
  SearchPreferences searchPreferences
) implements DomainEvent {

  public UserChangeStatusToAvailableEvent(UserChangeStatusTo output, SearchArea searchArea, SearchPreferences searchPreferences) {
    this(UUID.randomUUID(), Instant.now(), output, searchArea, searchPreferences);
  }
}
