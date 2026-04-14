package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.UserService;
import com.pairs.speed_dating.user.response.GetUserAgeAndGenderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/discovery")
@RequiredArgsConstructor
public class DiscoveryController {
  private final DiscoveryService discoveryService;
  private final UserService userService;

  @PostMapping("/refresh/{userId}")
  public ResponseEntity<List<UserProfile>> refresh(
    @PathVariable("userId") UUID userId,
    @RequestBody FilterAgeAndGenderRequest request
  ){
    GetUserAgeAndGenderResponse userInformation = userService.getUserAgeAndGender(userId);
    return ResponseEntity.status(HttpStatus.OK).body(discoveryService.handleFilterByAgeAndGender(
      userId,
      new FilterAgeAndGenderRequest(
        userInformation.age(),
        userInformation.gender(),
        request.localization(),
        request.filters()
      )
    ));
  }
}
