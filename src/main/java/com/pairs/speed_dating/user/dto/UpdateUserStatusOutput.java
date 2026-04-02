package com.pairs.speed_dating.user.dto;

import com.pairs.speed_dating.user.Gender;
import com.pairs.speed_dating.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateUserStatusOutput {
  private UUID userId;
  private UserStatus userStatus;
  private int age;
  private Gender gender;
}
