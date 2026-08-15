package com.pairs.speed_dating.user.repository;

import com.pairs.speed_dating.user.domain.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  @Modifying
  @Query("update RefreshTokenEntity r set r.revokedAt = :now where r.userId = :userId and r.revokedAt is null")
  void revokeAllActiveForUser(UUID userId, Instant now);
}
