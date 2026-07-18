package com.pairs.speed_dating.core.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher implements EventPublisher{
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publish(DomainEvent event){
    applicationEventPublisher.publishEvent(event);
  }
}
