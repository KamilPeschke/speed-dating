package com.pairs.speed_dating.core.event;

public interface EventPublisher {
  void publish(DomainEvent event);
}
