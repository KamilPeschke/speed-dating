package com.pairs.speed_dating.user.auth;

import java.util.UUID;

public record RotationResult(UUID userId, String newRawToken) {
}
