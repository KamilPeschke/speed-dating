package com.pairs.speed_dating.user.event;

import com.pairs.speed_dating.core.event.DomainEvent;
import com.pairs.speed_dating.user.api.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserChangeStatusToUnavailable(
  UUID eventId,
  Instant occurredOn,
  UUID userID,
  UserStatus userStatus
)implements DomainEvent {
  public UserChangeStatusToUnavailable(UUID userID, UserStatus userStatus){
    this(UUID.randomUUID(), Instant.now(), userID, userStatus);
  }
}
