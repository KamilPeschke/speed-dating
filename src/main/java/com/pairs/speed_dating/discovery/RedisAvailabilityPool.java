package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.discovery.dto.FilterAgeAndGenderRequest;
import com.pairs.speed_dating.discovery.dto.UserProfile;
import com.pairs.speed_dating.discovery.internal.DiscoveryService;
import com.pairs.speed_dating.discovery.internal.Filters;
import com.pairs.speed_dating.discovery.internal.LocalizationWithRadius;
import com.pairs.speed_dating.user.event.SearchArea;
import com.pairs.speed_dating.user.event.SearchPreferences;
import com.pairs.speed_dating.user.event.UserChangeStatusToAvailable;
import com.pairs.speed_dating.user.event.UserChangeStatusToUnavailable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAvailabilityPool {
  private final DiscoveryService discoveryService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private static final String HANDLE_AVAILABLE_USER_STATUS_CHANGE_EVENT_PATH = "queue/userStatusChange";

  @EventListener
  public void add(UserChangeStatusToAvailable userStatusChange){
    LocalizationWithRadius localization = toLocalization(userStatusChange.searchArea());
    Filters filters = toFilters(userStatusChange.searchPreferences());

    discoveryService.addUserToPoolAfterStatusChanges(
      userStatusChange.output().userId(),
      localization,
      filters,
      userStatusChange.output().age(),
      userStatusChange.output().gender()
    );

    List<UserProfile> users = discoveryService.handleFilterByAgeAndGender(
      userStatusChange.output().userId(),
      new FilterAgeAndGenderRequest(
        userStatusChange.output().age(),
        userStatusChange.output().gender(),
        localization,
        filters
      )
    );

    simpMessagingTemplate.convertAndSendToUser(
      userStatusChange.output().userId().toString(),
      HANDLE_AVAILABLE_USER_STATUS_CHANGE_EVENT_PATH,
      users
    );
  }

  @EventListener
  public void remove(UserChangeStatusToUnavailable userChangeStatusToUnavailable){
    discoveryService.removeUserFromPoolAfterStatusChanges(
      userChangeStatusToUnavailable.userID()
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
