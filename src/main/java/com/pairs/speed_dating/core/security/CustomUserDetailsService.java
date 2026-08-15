package com.pairs.speed_dating.core.security;

import com.pairs.speed_dating.user.api.UserProfileProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserProfileProvider userProfileProvider;

  @Override
  public UserDetails loadUserByUsername(@NonNull String username){
    return userProfileProvider.loadUserByUsername(username);
  }
}
