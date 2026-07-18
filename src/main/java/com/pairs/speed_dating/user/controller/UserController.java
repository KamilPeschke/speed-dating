package com.pairs.speed_dating.user;

import com.pairs.speed_dating.user.dto.*;
import com.pairs.speed_dating.user.response.CreateUserResponse;
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

  @PostMapping("/login")
  public ResponseEntity<UUID> loginUser(@Valid @RequestBody LoginDto credentials) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.login(credentials));
  }
  //TODO take userId from token JWT/handshake
  @PostMapping("/{id}/status-available")
  public ResponseEntity<UpdateUserStatus> updateUserStatusToAvailable(
    @PathVariable("id") UUID userId,
    @Valid @RequestBody UpdateUserStatusToAvailableDto userStatus) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatusToAvailable(userId, userStatus));
  }

  @PostMapping("/{id}/status-unavailable")
  public ResponseEntity<UpdateUserStatus> updateUserStatusToUnavailable(@PathVariable("id") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatusToUnavailable(userId));
  }
}
