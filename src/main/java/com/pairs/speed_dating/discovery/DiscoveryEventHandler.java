package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.event.UserChangeStatusEvent;
import com.pairs.speed_dating.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class DiscoveryEventHandler {
  private final DiscoveryService discoveryService;

  @EventListener
  public void handleUserStatusChange(UserChangeStatusEvent event){
    if(event.output().getUserStatus() == UserStatus.AVAILABLE) {
      discoveryService.addUserToPoolAfterStatusChanges(
        event.output().getUserId(),
        event.output().getUserStatus(),
        event.localization(),
        event.filters()
      );

      discoveryService.handleFilterByAgeAndGender(
        event.output().getUserId(),
        event.output().getAge(),
        event.output().getGender(),
        event.localization(),
        event.filters()
      );

    } else if(event.output().getUserStatus() == UserStatus.UNAVAILABLE){
      discoveryService.removeUserFromPoolAfterStatusChanges(
        event.output().getUserId(),
        event.output().getUserStatus()
      );

      log.info("User {} has been removed", event.output().getUserId());
    }
  }
}
