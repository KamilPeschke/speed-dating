package com.pairs.speed_dating.user.internal;

import com.pairs.speed_dating.core.exception.UserAlreadyExistsException;
import com.pairs.speed_dating.core.exception.UserNotFoundException;
import com.pairs.speed_dating.core.event.DomainEventPublisher;
import com.pairs.speed_dating.user.api.UserAgeAndGender;
import com.pairs.speed_dating.user.api.UserChangeStatusTo;
import com.pairs.speed_dating.user.api.UserProfileProvider;
import com.pairs.speed_dating.user.api.UserProfileWithoutDistance;
import com.pairs.speed_dating.user.dto.response.UpdateUserStatus;
import com.pairs.speed_dating.user.domain.UserEntity;
import com.pairs.speed_dating.user.api.UserStatus;
import com.pairs.speed_dating.user.dto.*;
import com.pairs.speed_dating.user.event.SearchArea;
import com.pairs.speed_dating.user.event.SearchPreferences;
import com.pairs.speed_dating.user.event.UserChangeStatusToAvailableEvent;
import com.pairs.speed_dating.user.event.UserChangeStatusToUnavailableEvent;
import com.pairs.speed_dating.user.repository.UserRepository;
import com.pairs.speed_dating.user.dto.response.CreateUserResponse;
import com.pairs.speed_dating.user.dto.response.GetUserProfileInformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserProfileProvider {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final DomainEventPublisher domainEventPublisher;

  @Transactional
  public CreateUserResponse createUser(CreateUserDto user) {

    if(userRepository.existsByEmail(user.email())){
      throw new UserAlreadyExistsException(user.email());
    }

    UserEntity userEntity = UserEntity.builder()
      .email(user.email())
      .password(passwordEncoder.encode(user.password()))
      .name(user.name())
      .surname(user.surname())
      .age(user.age())
      .gender(user.gender())
      .interestedIn(user.interestedIn())
      .createdAt(Date.from(java.time.Instant.now()))
      .build();

    UserEntity savedUser = userRepository.save(userEntity);

    log.info("Created user: {}", savedUser.getEmail());

    return new CreateUserResponse(
      savedUser.getEmail(),
      savedUser.getId()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public UserAgeAndGender getUserAgeAndGender(UUID userId) {
    UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->
      new UserNotFoundException(userId));
    return new UserAgeAndGender(
      userEntity.getAge(),
      userEntity.getGender()
    );
  }

  public GetUserProfileInformation getUserProfileInformation(UUID userId) {
    UserEntity user =  userRepository.findById(userId).orElseThrow(()->
      new UserNotFoundException(userId));
    return new GetUserProfileInformation(
      user.getAge(),
      user.getGender(),
      user.getInterestedIn(),
      user.getName()
    );
  }

  @Override
  public List<UserProfileWithoutDistance> getUserProfilesWithoutDistance(Set<UUID> nearbyUsersId) {
    return userRepository.findByIdInAndDeletedAtIsNullAndStatus(nearbyUsersId, UserStatus.AVAILABLE);
  }

  @Transactional
  public UpdateUserStatus updateUserStatusToAvailable(
    UUID userId,
    UpdateUserStatusToAvailableDto updateUserStatus
  ) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() ->
      new UserNotFoundException(userId));

    user.changeStatus(UserStatus.AVAILABLE);

    UserChangeStatusTo response = new UserChangeStatusTo(
      user.getId(),
      user.getStatus(),
      user.getAge(),
      user.getGender()
    );
    log.info("Publishing update user status event for user {} and status {}" , userId, user.getStatus());

    SearchArea searchArea = new SearchArea(
      updateUserStatus.localization().lat(),
      updateUserStatus.localization().lon(),
      updateUserStatus.localization().radiusKm()
    );

    SearchPreferences searchPreferences = new SearchPreferences(
      updateUserStatus.filters().ageFrom(),
      updateUserStatus.filters().ageTo(),
      updateUserStatus.filters().gender()
    );

    domainEventPublisher.publish(
      new UserChangeStatusToAvailableEvent(
        response,
        searchArea,
        searchPreferences
      )
    );

    return new UpdateUserStatus(user.getId(), user.getStatus());
  }

  @Transactional
  public UpdateUserStatus updateUserStatusToUnavailable(
    UUID userId
  ) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() ->
      new UserNotFoundException(userId));

    user.changeStatus(UserStatus.UNAVAILABLE);

    domainEventPublisher.publish(
      new UserChangeStatusToUnavailableEvent(
        userId,
        user.getStatus()
      )
    );

  return new UpdateUserStatus(userId, user.getStatus());
  }

  //TODO for future changes - only for testing purposes
  @Transactional(readOnly = true)
  public UUID login(LoginDto credentials) {
    UserEntity user = userRepository.findByEmail(credentials.email()).orElseThrow(() ->
      new UserNotFoundException(credentials.email()));
    if(!passwordEncoder.matches(credentials.password(), user.getPassword())){
      log.warn("Invalid credentials");
      throw new RuntimeException("Invalid credentials");
    }
    return user.getId();
  }
}
