package com.pairs.speed_dating.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class RefreshTokenEntity {
  @Id @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, unique = true)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column
  private Instant revokedAt;

  @Builder
  public RefreshTokenEntity(UUID userId, String tokenHash, Instant expiresAt, Instant createdAt){
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public void revoke(){
    this.revokedAt = Instant.now();
  }

  public boolean isRevoked(){
    return revokedAt != null;
  }

  public boolean isExpired(){
    return expiresAt.isBefore(Instant.now());
  }
}
