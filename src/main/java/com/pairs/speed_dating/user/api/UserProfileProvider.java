package com.pairs.speed_dating.user.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserProfileProvider {

  UserAgeAndGender getUserAgeAndGender(UUID userId);

  List<UserProfileWithoutDistance> getUserProfilesWithoutDistance(Set<UUID> userIds);
}
