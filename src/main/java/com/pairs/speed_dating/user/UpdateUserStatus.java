package com.pairs.speed_dating.user;
import java.util.UUID;

//TODO change naming convection
public record UpdateUserStatus(UUID userId, UserStatus userStatus) {}
