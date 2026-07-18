package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.domain.Gender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record UserProfileWithoutDistance(
  UUID id,
  String name,
  int age,
  Gender gender,
  String profilePhotoLink,
  List<String> photos
) {
  public UserProfileWithoutDistance {
    if(photos == null){
      photos = Collections.emptyList();
    }
  }
}
