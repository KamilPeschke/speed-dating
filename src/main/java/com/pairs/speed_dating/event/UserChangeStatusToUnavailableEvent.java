package com.pairs.speed_dating.event;

import com.pairs.speed_dating.user.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserChangeStatusToUnavailableEvent(
  UUID eventId,
  Instant occurredOn,
  UUID userID,
  UserStatus userStatus
)implements DomainEvent {
  public UserChangeStatusToUnavailableEvent(UUID userID, UserStatus userStatus){
    this(UUID.randomUUID(), Instant.now(), userID, userStatus);
  }
}
