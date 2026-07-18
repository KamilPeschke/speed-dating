package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.domain.Gender;
import com.pairs.speed_dating.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateUserStatusResponse {
  private UUID userId;
  private UserStatus userStatus;
  private Integer age;
  private Gender gender;
}
