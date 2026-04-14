package com.pairs.speed_dating.event;

public interface EventPublisher {
  void publish(DomainEvent event);
}
