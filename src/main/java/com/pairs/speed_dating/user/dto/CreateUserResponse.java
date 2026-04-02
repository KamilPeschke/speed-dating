package com.pairs.speed_dating.user.dto;

import java.util.UUID;

public record CreateUserResponse(String email, UUID uuid) {}
