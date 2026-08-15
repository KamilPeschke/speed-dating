package com.pairs.speed_dating.user.auth;

import com.pairs.speed_dating.user.dto.response.CreateUserResponse;

public record AuthResult(CreateUserResponse body, String rawRefreshToken) {
}
