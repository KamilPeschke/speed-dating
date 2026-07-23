package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.discovery.dto.FilterAgeAndGenderRequest;
import com.pairs.speed_dating.discovery.dto.UserProfile;
import com.pairs.speed_dating.discovery.internal.DiscoveryService;
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

  @PostMapping("/refresh/{userId}")
  public ResponseEntity<List<UserProfile>> refresh(
    @PathVariable("userId") UUID userId,
    @RequestBody FilterAgeAndGenderRequest request
  ){
    return ResponseEntity.status(HttpStatus.OK).body(discoveryService.refresh(userId, request));
  }
}
