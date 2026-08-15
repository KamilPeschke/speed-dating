package com.pairs.speed_dating.user.controller;

import com.pairs.speed_dating.user.auth.AuthService;
import com.pairs.speed_dating.user.auth.AuthenticatedUserPrincipal;
import com.pairs.speed_dating.user.dto.response.UpdateUserStatus;
import com.pairs.speed_dating.user.dto.*;
import com.pairs.speed_dating.user.dto.response.CreateUserResponse;
import com.pairs.speed_dating.user.dto.response.GetUserProfileInformation;
import com.pairs.speed_dating.user.internal.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final AuthService authService;
  private final static String REFRESH_COOKIE = "refresh_token";

  private ResponseCookie buildRefreshCookie(String raw) {
    return ResponseCookie.from(REFRESH_COOKIE, raw)
      .httpOnly(true)
      .secure(false) //local environment
      .sameSite("Strict")
      .path("/api/user")
      .maxAge(Duration.ofDays(7))
      .build();
  }

  private ResponseCookie clearRefreshCookie() {
    return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(true)
      .sameSite("Strict").path("/api/user").maxAge(0).build();
  }

  @PostMapping("/create")
  public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserDto user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
  }

  @PostMapping("/login")
  public ResponseEntity<CreateUserResponse> loginUser(@Valid @RequestBody LoginDto credentials) {
    var result = authService.login(credentials);
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.rawRefreshToken()).toString())
      .body(result.body());
  }

  @PostMapping("/refresh")
  public ResponseEntity<CreateUserResponse> refresh(
    @CookieValue(name = REFRESH_COOKIE, required = false) String rawRefresh) {
    if (rawRefresh == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    var result = authService.refresh(rawRefresh);
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.rawRefreshToken()).toString())
      .body(result.body());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String rawRefresh) {
    if (rawRefresh != null) authService.logout(rawRefresh);
    return ResponseEntity.noContent()
      .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
      .build();
  }

  @GetMapping("/info")
  public ResponseEntity<GetUserProfileInformation> getUserProfileInformation(@AuthenticationPrincipal AuthenticatedUserPrincipal principal){
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUserProfileInformation(principal.id()));
  }

  @PostMapping("/status-available")
  public ResponseEntity<UpdateUserStatus> updateUserStatusToAvailable(
    @AuthenticationPrincipal() AuthenticatedUserPrincipal principal,
    @Valid @RequestBody UpdateUserStatusToAvailableDto userStatus) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatusToAvailable(principal.id(), userStatus));
  }

  @PostMapping("/status-unavailable")
  public ResponseEntity<UpdateUserStatus> updateUserStatusToUnavailable(@AuthenticationPrincipal() AuthenticatedUserPrincipal principal) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatusToUnavailable(principal.id()));
  }
}
