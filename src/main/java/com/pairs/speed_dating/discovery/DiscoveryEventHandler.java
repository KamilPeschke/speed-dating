package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.event.UserChangeStatusToAvailableEvent;
import com.pairs.speed_dating.event.UserChangeStatusToUnavailableEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DiscoveryEventHandler {
  private final DiscoveryService discoveryService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private static final String HANDLE_AVAILABLE_USER_STATUS_CHANGE_EVENT_PATH = "queue/userStatusChange";

  @EventListener
  public void handleUserStatusChangeToAvailable(UserChangeStatusToAvailableEvent event){
    discoveryService.addUserToPoolAfterStatusChanges(
      event.output().getUserId(),
      event.output().getUserStatus(),
      event.localization(),
      event.filters(),
      event.output().getAge(),
      event.output().getGender()
    );

    List<UserProfile> users = discoveryService.handleFilterByAgeAndGender(
      event.output().getUserId(),
      //TODO we can change this to different record using MapStruct
      new FilterAgeAndGenderRequest(
        event.output().getAge(),
        event.output().getGender(),
        event.localization(),
        event.filters()
      )
    );

    simpMessagingTemplate.convertAndSendToUser(
      event.output().getUserId().toString(),
      HANDLE_AVAILABLE_USER_STATUS_CHANGE_EVENT_PATH,
      users
    );
  }

  @EventListener
  public void handleUserStatusChangeToUnavailable(UserChangeStatusToUnavailableEvent event){
    discoveryService.removeUserFromPoolAfterStatusChanges(
      event.userID(),
      event.userStatus()
    );
  }
}
