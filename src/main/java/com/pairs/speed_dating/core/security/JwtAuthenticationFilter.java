package com.pairs.speed_dating.core.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String username = jwtService.extractUsername(token);

      if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(token, userDetails)) {
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
