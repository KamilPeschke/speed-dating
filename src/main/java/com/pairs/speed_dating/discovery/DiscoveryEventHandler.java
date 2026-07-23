package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.discovery.dto.FilterAgeAndGenderRequest;
import com.pairs.speed_dating.discovery.dto.UserProfile;
import com.pairs.speed_dating.discovery.internal.DiscoveryService;
import com.pairs.speed_dating.discovery.internal.Filters;
import com.pairs.speed_dating.discovery.internal.LocalizationWithRadius;
import com.pairs.speed_dating.user.event.SearchArea;
import com.pairs.speed_dating.user.event.SearchPreferences;
import com.pairs.speed_dating.user.event.UserChangeStatusToAvailableEvent;
import com.pairs.speed_dating.user.event.UserChangeStatusToUnavailableEvent;
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
    LocalizationWithRadius localization = toLocalization(event.searchArea());
    Filters filters = toFilters(event.searchPreferences());

    discoveryService.addUserToPoolAfterStatusChanges(
      event.output().userId(),
      localization,
      filters,
      event.output().age(),
      event.output().gender()
    );

    List<UserProfile> users = discoveryService.handleFilterByAgeAndGender(
      event.output().userId(),
      new FilterAgeAndGenderRequest(
        event.output().age(),
        event.output().gender(),
        localization,
        filters
      )
    );

    simpMessagingTemplate.convertAndSendToUser(
      event.output().userId().toString(),
      HANDLE_AVAILABLE_USER_STATUS_CHANGE_EVENT_PATH,
      users
    );
  }

  @EventListener
  public void handleUserStatusChangeToUnavailable(UserChangeStatusToUnavailableEvent event){
    discoveryService.removeUserFromPoolAfterStatusChanges(
      event.userID()
    );
  }

  private LocalizationWithRadius toLocalization(SearchArea searchArea) {
    return new LocalizationWithRadius(
      searchArea.lat(),
      searchArea.lon(),
      searchArea.radiusKm()
    );
  }

  private Filters toFilters(SearchPreferences searchPreferences) {
    return new Filters(
      searchPreferences.ageFrom(),
      searchPreferences.ageTo(),
      searchPreferences.gender()
    );
  }
}
