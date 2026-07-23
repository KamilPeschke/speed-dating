package com.pairs.speed_dating.discovery.dto;

import com.pairs.speed_dating.user.api.Gender;
import java.util.List;
import java.util.UUID;

public record UserProfile (
  UUID userId,
  String name,
  Integer age,
  Gender gender,
  Double distance,
  String profilePhotoLink,
  List<String> photos
){}
