package com.pairs.speed_dating.core.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
  UUID eventId();
  Instant occurredOn();
}
