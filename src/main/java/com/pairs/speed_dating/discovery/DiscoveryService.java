package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.redis.GeoQueryResult;
import com.pairs.speed_dating.redis.LocalizationWithRadius;
import com.pairs.speed_dating.redis.RedisService;
import com.pairs.speed_dating.user.Filters;
import com.pairs.speed_dating.user.Gender;
import com.pairs.speed_dating.user.UserRepository;
import com.pairs.speed_dating.user.UserStatus;
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
  private final UserRepository userRepository;

  public List<UserProfile> handleFilterByAgeAndGender(
    UUID userId,
    int age,
    Gender gender,
    LocalizationWithRadius localization,
    Filters filters
  ){
    List<GeoQueryResult> nearbyUsers = redisService.findNearbyUsers(localization);
    List<GeoQueryResult> filterResult = nearbyUsers.stream()
      .filter(u -> matchNearbyUsersWithFilters(u.getUserId(), gender, age))
      .filter(u -> !userId.equals(u.getUserId()))
      .toList();

    if(filterResult.isEmpty()){
      log.info("No nearby users found, return empty list, userId: {}", userId);
      return List.of();
    }

    log.info("Found {} nearby users found, userId: {}", filterResult.size(), userId);

    Map<UUID, Double> distanceMap = filterResult.stream()
      .collect(Collectors.toMap(
        GeoQueryResult::getUserId,
        GeoQueryResult::getDistance
      ));

    Set<UUID> nearbyUsersId = distanceMap.keySet();

    //Getting a full profile of our users with pictures
    List<UserProfileWithoutDistance> userProfileWithoutDistance = userRepository.findByIdInAndDeletedAtIsNullAndStatus(nearbyUsersId, UserStatus.AVAILABLE);

    List<UserProfile> userProfilesWithDistance = userProfileWithoutDistance.stream()
      .map(u -> {
        double distance = distanceMap.get(u.id());
        return UserProfile.builder()
          .userId(u.id())
          .name(u.name())
          .age(u.age())
          .gender(u.gender())
          .distance(distance)
          .profilePhotoLink(u.profilePhotoLink())
          .photos(u.photos())
          .build();
      })
    .toList();

    log.info("Streaming list of users for {}" , userId);

    return filterNearbyUserByAgeAndGender(filters, userProfilesWithDistance);
  }

  private boolean matchNearbyUsersWithFilters(
    UUID userId,
    Gender gender,
    int age
  ){
    Filters nearbyUserFilters = redisService.getFilters(userId);
    if(nearbyUserFilters.gender() == gender){
      return nearbyUserFilters.ageFrom() <= age && nearbyUserFilters.ageTo() >= age;
    }
    return false;
  }

  private List<UserProfile> filterNearbyUserByAgeAndGender(
    Filters filters,
    List<UserProfile> userProfiles
  ){
    return userProfiles.stream().filter(u ->
        filters.ageFrom() <= u.getAge() &&
        filters.ageTo() >= u.getAge() &&
        filters.gender() == u.getGender()
    ).toList();
  }

  public UserStatus addUserToPoolAfterStatusChanges(
    UUID userId,
    UserStatus status,
    LocalizationWithRadius localization,
    Filters filters
  ){
    if(status == null){
      log.error("Status cannot be null");
      throw new IllegalArgumentException("Status cannot be null");
    }
      redisService.addUserToPool(
        userId,
        localization.lon(),
        localization.lat(),
        filters
      );

    log.info("User {} has been added to the pool", userId);
    return status;
  }

  public UserStatus removeUserFromPoolAfterStatusChanges(
    UUID userId,
    UserStatus userStatus
  ){
    if(userStatus == null || userId == null){
      log.error("Status or userId cannot be null");
      throw new IllegalArgumentException("Status cannot be null");
    }
    redisService.removeUserFromAvailablePool(userId);
    log.info("User {} has been removed from the pool", userId);
    return userStatus;
  }
}
