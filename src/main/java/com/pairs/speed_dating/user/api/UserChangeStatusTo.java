package com.pairs.speed_dating.user.api;

import java.util.UUID;

public record UserChangeStatusTo(
  UUID userId,
  UserStatus userStatus,
  Integer age,
  Gender gender
) {
}
