package com.pairs.speed_dating.user.response;

import com.pairs.speed_dating.user.Gender;
import com.pairs.speed_dating.user.UserStatus;
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
