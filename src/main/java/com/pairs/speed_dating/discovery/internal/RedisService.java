package com.pairs.speed_dating.discovery.internal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pairs.speed_dating.user.api.Gender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;

import org.springframework.stereotype.Service;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

  private static final String AVAILABLE_USERS_KEY = "available_users";
  private static final String USER_DATA_PREFIX = "user_data:";
  private static final int USER_DATA_TTL_SECONDS = 3600;
  private final ObjectMapper objectMapper;
  private final RedisClient redisClient;

  public void addUserToPool(
    UUID userId,
    double lon,
    double lat,
    int currentUserAge,
    Gender currentUserGender,
    Filters filters
  ){
    try (Pipeline pipeline = redisClient.pipelined()) {
      //TODO geoadd need another method for deleting keys after some time - setex have this feature but not geoadd (TTL).
      pipeline.geoadd(AVAILABLE_USERS_KEY, lon, lat, String.valueOf(userId));

      FiltersAndBasicData filtersAndBasicData = new FiltersAndBasicData(
        filters,
        currentUserAge,
        currentUserGender
      );
      String userData = objectMapper.writeValueAsString(filtersAndBasicData);

      pipeline.setex(USER_DATA_PREFIX + userId, USER_DATA_TTL_SECONDS, userData);
      pipeline.sync();

    } catch (Exception error) {
      log.error("Error while adding a user: {} to pool", userId, error);
      throw new RuntimeException("Error while adding a user");
    }
  }

  public void removeUserFromAvailablePool(UUID userId) {
    try (Pipeline pipeline = redisClient.pipelined()) {
      pipeline.zrem(AVAILABLE_USERS_KEY, userId.toString());
      pipeline.del(USER_DATA_PREFIX + userId);

      pipeline.sync();
    } catch (Exception error) {
      log.error("Error while removing a user: {} from pool", userId, error);
      throw new RuntimeException("Error while removing user from Redis pool");
    }
  }

  public List<GeoQueryResult> findNearbyUsers(LocalizationWithRadius localization){
    try {
      GeoSearchParam searchParams = new GeoSearchParam()
        .fromLonLat(localization.lon(), localization.lat())
        .byRadius(localization.radiusKm(), GeoUnit.KM)
        .withDist();

      List<GeoRadiusResponse> nearbyUsers = redisClient.geosearch(
        AVAILABLE_USERS_KEY,
        searchParams
      );

      return nearbyUsers.stream()
        .map(res -> new GeoQueryResult(
          UUID.fromString(res.getMemberByString()),
          res.getDistance()
        )).toList();
    } catch (Exception error) {
      log.error("Error while finding nearby users", error);
      throw new RuntimeException("Error while finding nearby users");
    }
  }

  @SneakyThrows
  public Map<UUID, FiltersAndBasicData> getFilters(List<UUID> userId) {
    if(userId == null || userId.isEmpty()){
      return Map.of();
    }

    List<String> userIdWithPrefix = userId.stream()
      .map(UUID::toString)
      .map(u -> USER_DATA_PREFIX + u)
      .toList();

    List<String> userData = redisClient.mget(userIdWithPrefix.toArray(new String[0]));
    Map<UUID, FiltersAndBasicData> filtersMap = new HashMap<>();

    for(int i=0; i<=userData.size() - 1; i++){
      String currentUserData = userData.get(i);
      if(currentUserData == null){
        continue;
      }
      FiltersAndBasicData filtersAndBasicData = objectMapper.readValue(currentUserData, FiltersAndBasicData.class);
      filtersMap.put(userId.get(i), filtersAndBasicData);
    }
    return filtersMap;
  }
}
