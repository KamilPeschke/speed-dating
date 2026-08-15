package com.pairs.speed_dating.user.auth;

import com.pairs.speed_dating.core.exception.UserAlreadyExistsException;
import com.pairs.speed_dating.core.exception.UserNotFoundException;
import com.pairs.speed_dating.core.security.JwtService;
import com.pairs.speed_dating.core.security.UserClaims;
import com.pairs.speed_dating.user.domain.UserEntity;
import com.pairs.speed_dating.user.dto.CreateUserDto;
import com.pairs.speed_dating.user.dto.LoginDto;
import com.pairs.speed_dating.user.dto.response.CreateUserResponse;
import com.pairs.speed_dating.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
  private static final String TOKEN_TYPE = "Bearer";
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public CreateUserResponse register(CreateUserDto user) {
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
      savedUser.getId(),
      jwtService.generateToken(new UserClaims(savedUser.getId(), savedUser.getEmail())),
      TOKEN_TYPE,
      jwtService.getExpirationMs()
    );
  }

  @Transactional(readOnly = true)
  public AuthResult login(LoginDto credentials) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(credentials.email(), credentials.password())
    );
    AuthenticatedUserPrincipal principal = (AuthenticatedUserPrincipal) authentication.getPrincipal();
    String access = jwtService.generateToken(new UserClaims(principal.id(), principal.email()));
    String refresh = refreshTokenService.issue(principal.id());
    log.info("User: {} is authenticated, login.", principal.id());
    return new AuthResult(new CreateUserResponse(
      principal.email(),
      principal.id(),
      access,
      TOKEN_TYPE,
      jwtService.getExpirationMs()
    ), refresh);
  }

  @Transactional
  public AuthResult refresh(String rawRefreshToken){
    var result = refreshTokenService.rotate(rawRefreshToken);
    UserEntity user = userRepository.findById(result.userId())
      .orElseThrow(() -> new UserNotFoundException(result.userId()));

    String access = jwtService.generateToken(new UserClaims(user.getId(), user.getEmail()));
    return new AuthResult(new CreateUserResponse(
      user.getEmail(),
      user.getId(),
      access,
      TOKEN_TYPE,
      jwtService.getExpirationMs()),
      result.newRawToken());
  }

  public void logout(String rawRefreshToken){
    refreshTokenService.revoke(rawRefreshToken);
  }
}
