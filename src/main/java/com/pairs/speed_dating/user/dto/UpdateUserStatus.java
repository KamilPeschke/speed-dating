package com.pairs.speed_dating.user.dto;
import com.pairs.speed_dating.user.UserStatus;
import java.util.UUID;

public record UpdateUserStatus(UUID userId, UserStatus userStatus) {}
