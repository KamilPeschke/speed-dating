package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.dto.CreateUserDto;
import com.pairs.speed_dating.user.dto.CreateUserResponse;
import com.pairs.speed_dating.user.dto.UpdateUserStatus;
import com.pairs.speed_dating.user.dto.UpdateUserStatusDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/create")
  public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserDto user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
  }

  @PostMapping("/{id}/status")
  public ResponseEntity<UpdateUserStatus>  updateUserStatus(
    @PathVariable("id") UUID userId,
    @RequestBody UpdateUserStatusDto userStatus) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatus(userId, userStatus));
  }
}
