package com.pairs.speed_dating.user.dto.response;

import java.util.UUID;

public record LoginUserResponse(
  String email,
  UUID uuid,
  String token,
  String tokenType,
  long expiresInMs
) {}
