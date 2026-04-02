package com.pairs.speed_dating.user;

import com.pairs.speed_dating.core.exception.UserAlreadyExistsException;
import com.pairs.speed_dating.event.UserChangeStatusEvent;
import com.pairs.speed_dating.core.exception.UserNotFoundException;
import com.pairs.speed_dating.redis.LocalizationWithCoordinates;
import com.pairs.speed_dating.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Transactional
  public CreateUserResponse createUser(CreateUserDto user) {

    if(userRepository.existsByEmail(user.getEmail())){
      throw new UserAlreadyExistsException(user.getEmail());
    };

    UserEntity userEntity = UserEntity.builder()
      .email(user.getEmail())
      .password(passwordEncoder.encode(user.getPassword()))
      .name(user.getName())
      .surname(user.getSurname())
      .age(user.getAge())
      .gender(user.getGender())
      .interestedIn(user.getInterestedIn())
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
  public UpdateUserStatus updateUserStatus(
    UUID userId,
    UpdateUserStatusDto updateUserStatus
  ) {
    log.info("Updating user status for user with id {} and status {}", userId, updateUserStatus.getStatus());
    UserEntity user = userRepository.findById(userId).orElseThrow(() ->
      new UserNotFoundException(userId));

    user.changeStatus(updateUserStatus.getStatus());

    UpdateUserStatusOutput output = new UpdateUserStatusOutput(
      user.getId(),
      user.getStatus(),
      user.getAge(),
      user.getGender()
    );
    log.info("Publishing update user status event for user {} and status {}" , userId, user.getStatus());

    applicationEventPublisher.publishEvent(
      new UserChangeStatusEvent(
        output,
        new LocalizationWithCoordinates(
          updateUserStatus.getLocalization().getLat(),
          updateUserStatus.getLocalization().getLon()
        ),
        updateUserStatus.getFilters()
      )
    );

    return new UpdateUserStatus(user.getId(), user.getStatus());
  }
}
