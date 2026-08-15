package com.pairs.speed_dating.user.auth;

import com.pairs.speed_dating.user.domain.RefreshTokenEntity;
import com.pairs.speed_dating.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository repository;
  private final RefreshTokenHasher hasher;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  @Transactional
  public String issue(UUID userId){
    String raw = hasher.generateRawToken();
    repository.save(RefreshTokenEntity.builder()
      .userId(userId)
      .tokenHash(hasher.hash(raw))
      .createdAt(Instant.now())
      .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
      .build());
    return raw;
  }

  @Transactional
  public RotationResult rotate(String rawToken){
    RefreshTokenEntity token = repository.findByTokenHash(hasher.hash(rawToken))
      .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    if(token.isRevoked()){
      repository.revokeAllActiveForUser(token.getUserId(), Instant.now());
      throw new BadCredentialsException("Refresh token reuse detected");
    }
    if(token.isExpired()){
      throw new BadCredentialsException("Refresh token expired");
    }

    token.revoke();
    String newRaw = issue(token.getUserId());
    return new RotationResult(token.getUserId(), newRaw);
  }

  @Transactional
  public void revoke(String rawToken){
    repository.findByTokenHash(hasher.hash(rawToken))
      .ifPresent(RefreshTokenEntity::revoke);
  }
}
