package com.pairs.speed_dating.user.auth;

import com.pairs.speed_dating.user.Role;
import com.pairs.speed_dating.user.domain.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUserPrincipal(
  UUID id,
  String email,
  String password,
  Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

  public static AuthenticatedUserPrincipal fromUser(UserEntity user) {
    Role role = user.getRole() != null ? user.getRole() : Role.USER;
    return new AuthenticatedUserPrincipal(
      user.getId(),
      user.getEmail(),
      user.getPassword(),
      List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
    );
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }
}