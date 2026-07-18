package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.domain.Gender;
import com.pairs.speed_dating.user.domain.UserStatus;

import java.util.UUID;

public record UserChangeStatusTo(
  UUID userId,
  UserStatus userStatus,
  Integer age,
  Gender gender
) {
}
