package com.pairs.speed_dating.user.internal;

import com.pairs.speed_dating.core.exception.UserNotFoundException;
import com.pairs.speed_dating.user.api.*;
import com.pairs.speed_dating.user.auth.AuthenticatedUserPrincipal;
import com.pairs.speed_dating.user.dto.response.UpdateUserStatus;
import com.pairs.speed_dating.user.domain.UserEntity;
import com.pairs.speed_dating.user.dto.*;
import com.pairs.speed_dating.user.event.SearchArea;
import com.pairs.speed_dating.user.event.SearchPreferences;
import com.pairs.speed_dating.user.event.UserChangeStatusToAvailable;
import com.pairs.speed_dating.user.event.UserChangeStatusToUnavailable;
import com.pairs.speed_dating.user.repository.UserRepository;
import com.pairs.speed_dating.user.dto.response.GetUserProfileInformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserProfileProvider {
  private final UserRepository userRepository;
  private final UserAvailabilityPool userAvailabilityPool;

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

    userAvailabilityPool.add(new UserChangeStatusToAvailable(
      response,
      searchArea,
      searchPreferences
    ));

    return new UpdateUserStatus(user.getId(), user.getStatus());
  }

  @Transactional
  public UpdateUserStatus updateUserStatusToUnavailable(
    UUID userId
  ) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() ->
      new UserNotFoundException(userId));

    user.changeStatus(UserStatus.UNAVAILABLE);

    userAvailabilityPool.remove(
      new UserChangeStatusToUnavailable(
        userId,
        user.getStatus()
      )
    );

  return new UpdateUserStatus(userId, user.getStatus());
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByEmail(username)
      .orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found."));

    return AuthenticatedUserPrincipal.fromUser(user);
  }
}
