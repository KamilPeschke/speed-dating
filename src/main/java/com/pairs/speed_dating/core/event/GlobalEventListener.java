package com.pairs.speed_dating.core.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class GlobalEventListener {

  @EventListener
  public void logEvents(DomainEvent event){
    log.info("Domain Event published: [Type={}] [ID={}] [OccurredAt={}] [Payload={}]",
      event.getClass().getSimpleName(),
      event.eventId(),
      event.occurredOn(),
      event
    );
  }
}
