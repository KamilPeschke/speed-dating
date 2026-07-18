package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.UserProfileWithoutDistance;
import com.pairs.speed_dating.user.LocalizationWithRadius;
import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.UserService;
import com.pairs.speed_dating.user.domain.Gender;
import com.pairs.speed_dating.user.repository.UserRepository;
import com.pairs.speed_dating.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscoveryService {
  private final RedisService redisService;
  private final UserService userService;

  public List<UserProfile> handleFilterByAgeAndGender(
    UUID userId,
    FilterAgeAndGenderRequest userFilters
  ){
    //TODO Temporary solution (DTO)
    if(userId == null || userFilters == null || userFilters.userAge() == null || userFilters.userGender() == null){
      log.error("Invalid data");
      throw new IllegalArgumentException("Invalid data");
    }

    List<GeoQueryResult> nearbyUsers = redisService.findNearbyUsers(userFilters.localization());
    List<UUID> usersIdForFiltering = nearbyUsers.stream().map(GeoQueryResult::getUserId).toList();
    Map<UUID, FiltersAndBasicData> profilesWhereUserMatchCriteria = checkIfUserMatchOtherUsersCriteria(
      usersIdForFiltering,
      userFilters.userGender(),
      userFilters.userAge()
    );

    if(profilesWhereUserMatchCriteria.isEmpty()){
      log.info("No nearby users found, return empty list, userId: {}", userId);
      return List.of();
    }

    Set<UUID> nearbyUsersId = filterNearbyUserByAgeAndGender(userFilters.filters(), profilesWhereUserMatchCriteria);

    if(nearbyUsersId.isEmpty()){
      log.info("No nearby users found, return empty list, userId: {}", userId);
      return List.of();
    }

    //Getting a full profile of our users with pictures
    List<UserProfileWithoutDistance> userProfileWithoutDistance = userService.getUserProfilesWithoutDistance(nearbyUsersId);

    if(userProfileWithoutDistance.isEmpty()){
      log.warn("[WARN] Can't find any profiles with provided ID's, returning empty list instead");
      return List.of();
    }

    Map<UUID, Double> distanceMap = nearbyUsers.stream()
      .filter(u -> u.getUserId() != null)
      .collect(Collectors.toMap(
        GeoQueryResult::getUserId,
        GeoQueryResult::getDistance
      ));

    List<UserProfile> userProfilesWithDistance = userProfileWithoutDistance.stream()
      .map(u -> {
        double distance = distanceMap.get(u.id());
        return new UserProfile(
          u.id(),
          u.name(),
          u.age(),
          u.gender(),
          distance,
          u.profilePhotoLink(),
          u.photos()
        );
      })
    .toList();

    log.info("Streaming list of users for userId: {}, number of users nearby {}" , userId, userProfilesWithDistance.size());

    return userProfilesWithDistance;
  }

  private Map<UUID, FiltersAndBasicData> checkIfUserMatchOtherUsersCriteria(
    List<UUID> userId,
    Gender userGender,
    int userAge
  ){
    Map<UUID, FiltersAndBasicData> nearbyUserFilters = redisService.getFilters(userId);
    return nearbyUserFilters.entrySet().stream()
      .filter(nearbyUser -> nearbyUser.getValue() != null &&
        nearbyUser.getValue().filters().gender() == userGender &&
        nearbyUser.getValue().filters().ageFrom() <= userAge &&
        nearbyUser.getValue().filters().ageTo() >= userAge)
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
      ));
  }

  private Set<UUID> filterNearbyUserByAgeAndGender(
    Filters userFilters,
    Map<UUID, FiltersAndBasicData> nearbyUsers
  ){
    return nearbyUsers.entrySet().stream()
      .filter(nearbyUserData -> nearbyUserData.getValue() != null &&
        userFilters.ageFrom() <= nearbyUserData.getValue().currentUserAge() &&
          userFilters.ageTo() >= nearbyUserData.getValue().currentUserAge()
        && userFilters.gender() == nearbyUserData.getValue().currentUserGender())
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
  }

  public void addUserToPoolAfterStatusChanges(
    UUID userId,
    UserStatus status,
    LocalizationWithRadius localization,
    Filters filters,
    int currentUserAge,
    Gender currentUserGender
  ){
    if(status == null){
      log.error("Status cannot be null");
      throw new IllegalArgumentException("Status cannot be null");
    }
      redisService.addUserToPool(
        userId,
        localization.lon(),
        localization.lat(),
        currentUserAge,
        currentUserGender,
        filters
      );

    log.info("User {} has been added to the pool", userId);
  }

  public void removeUserFromPoolAfterStatusChanges(
    UUID userId,
    UserStatus userStatus
  ){
    if(userStatus == null || userId == null){
      log.error("Status or userId cannot be null");
      throw new IllegalArgumentException("Invalid data");
    }
    redisService.removeUserFromAvailablePool(userId);
    log.info("User {} has been removed from the pool", userId);
  }
}
