package com.pairs.speed_dating.user;

import com.pairs.speed_dating.discovery.UserProfileWithoutDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  List<UserProfileWithoutDistance> findByIdInAndDeletedAtIsNullAndStatus(Set<UUID> id, UserStatus status);
}
