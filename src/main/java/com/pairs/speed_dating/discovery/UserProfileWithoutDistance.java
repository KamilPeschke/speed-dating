package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.Gender;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record UserProfileWithoutDistance(
  UUID id,
  String name,
  int age,
  Gender gender,
  @Nullable String profilePhotoLink,
  List<String> photos
) {
  public UserProfileWithoutDistance {
    if(photos == null){
      photos = Collections.emptyList();
    }
  }
}
