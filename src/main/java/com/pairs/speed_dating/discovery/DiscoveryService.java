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
import java.util.UUID;

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
    //TODO we can use Set collection; Redis already use sorted Set collection so we don't need to make sure about duplicates
    List<GeoQueryResult> nearbyUsers = redisService.findNearbyUsers(localization);
    List<GeoQueryResult> filerResult = nearbyUsers.stream()
      .filter(u -> matchNearbyUsersWithFilters(u.getUserId(), gender, age))
      .toList();

    if(filerResult.isEmpty()){
      log.info("No nearby users found, return empty list, userId: {}", userId);
      return List.of();
    }

    List<UUID> nearbyUsersId = filerResult.stream().map(GeoQueryResult::getUserId).toList();

    //Getting a full profile of our users with pictures
    List<UserProfile> userProfile = userRepository.findByIdInAndDeletedAtIsNullAndStatus(nearbyUsersId, UserStatus.AVAILABLE);
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

  private boolean filterNearbyUserByAgeAndGender(
    Filters filters,
    List<UserProfile> userProfiles
  ){

  }
}
