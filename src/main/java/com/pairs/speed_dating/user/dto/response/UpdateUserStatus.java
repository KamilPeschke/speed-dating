package com.pairs.speed_dating.user.dto.response;

import com.pairs.speed_dating.user.api.UserStatus;

import java.util.UUID;

//TODO change naming convection
public record UpdateUserStatus(UUID userId, UserStatus userStatus) {}
