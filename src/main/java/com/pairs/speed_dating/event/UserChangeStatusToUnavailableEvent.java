package com.pairs.speed_dating.event;

import com.pairs.speed_dating.user.UserStatus;

import java.util.UUID;

public record UserChangeStatusToUnavailableEvent(
  UUID userID,
  UserStatus userStatus
) {
}
