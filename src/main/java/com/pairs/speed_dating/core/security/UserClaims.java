package com.pairs.speed_dating.core.security;

import java.util.UUID;

public record UserClaims(UUID userId, String email) {
}
