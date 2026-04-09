package com.pairs.speed_dating.core.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(UUID userId) {
    super("User with id: " + userId.toString() + " not found");
  }

  public UserNotFoundException(String email) {
    super("User with id: " + email + " not found");
  }
}
