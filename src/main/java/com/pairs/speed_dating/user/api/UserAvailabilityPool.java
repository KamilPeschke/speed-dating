package com.pairs.speed_dating.user.api;

import com.pairs.speed_dating.user.event.UserChangeStatusToAvailable;
import com.pairs.speed_dating.user.event.UserChangeStatusToUnavailable;

public interface UserAvailabilityPool {
  void add(UserChangeStatusToAvailable  userChangeStatusToAvailable);
  void remove(UserChangeStatusToUnavailable userChangeStatusToUnavailableEvent);
}
