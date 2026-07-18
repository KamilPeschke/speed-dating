package com.pairs.speed_dating.user;

import com.pairs.speed_dating.core.exception.UserAlreadyExistsException;
import com.pairs.speed_dating.core.exception.UserNotFoundException;
import com.pairs.speed_dating.event.DomainEventPublisher;
import com.pairs.speed_dating.event.UserChangeStatusToAvailableEvent;
import com.pairs.speed_dating.event.UserChangeStatusToUnavailableEvent;
import com.pairs.speed_dating.redis.LocalizationWithRadius;
import com.pairs.speed_dating.user.dto.*;
import com.pairs.speed_dating.user.response.CreateUserResponse;
import com.pairs.speed_dating.user.response.GetUserAgeAndGenderResponse;
import com.pairs.speed_dating.user.response.UpdateUserStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final DomainEventPublisher domainEventPublisher;

  //TODO [DTO IN SERVICE LAYER] !
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

  @Transactional(readOnly = true)
  public GetUserAgeAndGenderResponse getUserAgeAndGender(UUID userId) {
    UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->
      new UserNotFoundException(userId));
    return new GetUserAgeAndGenderResponse(
      userEntity.getAge(),
      userEntity.getGender()
    );
  }

  @Transactional
  public UpdateUserStatus updateUserStatusToAvailable(
    UUID userId,
    UpdateUserStatusToAvailableDto updateUserStatus
  ) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() ->
      new UserNotFoundException(userId));

    user.changeStatus(UserStatus.AVAILABLE);

    UpdateUserStatusResponse response = new UpdateUserStatusResponse(
      user.getId(),
      user.getStatus(),
      user.getAge(),
      user.getGender()
    );
    log.info("Publishing update user status event for user {} and status {}" , userId, user.getStatus());

    //TODO change DTO mapping to MapStruct
    LocalizationWithRadius localization = new LocalizationWithRadius(
      updateUserStatus.localization().lat(),
      updateUserStatus.localization().lon(),
      updateUserStatus.localization().radiusKm()
    );

    Filters filters = new Filters(
      updateUserStatus.filters().ageFrom(),
      updateUserStatus.filters().ageTo(),
      updateUserStatus.filters().gender()
    );

    domainEventPublisher.publish(
      new UserChangeStatusToAvailableEvent(
        response,
        localization,
        filters
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
