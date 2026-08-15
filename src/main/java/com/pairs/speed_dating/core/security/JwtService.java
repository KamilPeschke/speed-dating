package com.pairs.speed_dating.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final long expirationMs;

  public JwtService(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.expiration-ms}") long expirationMs
  ) {
    this.signingKey = buildSigningKey(secret);
    this.expirationMs = expirationMs;
  }

  public String generateToken(UserClaims user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.userId());
    claims.put("email", user.email());

    return Jwts.builder()
      .claims(claims)
      .subject(user.email())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expirationMs))
      .signWith(signingKey)
      .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  public long getExpirationMs() {
    return expirationMs;
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = Jwts.parser()
      .verifyWith(signingKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();
    return claimsResolver.apply(claims);
  }

  private static SecretKey buildSigningKey(String secret) {
    String normalized = secret == null ? "" : secret.trim();
    byte[] keyBytes = tryDecodeBase64(normalized);

    if (keyBytes.length < 32) {
      keyBytes = normalized.getBytes(StandardCharsets.UTF_8);
    }
    if (keyBytes.length < 32) {
      throw new IllegalStateException(
        "JWT secret must be at least 32 bytes for HS256. Update app.jwt.secret / JWT_SECRET."
      );
    }
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private static byte[] tryDecodeBase64(String value) {
    try {
      return Decoders.BASE64.decode(value);
    } catch (RuntimeException exception) {
      return new byte[0];
    }
  }
}
