package com.pairs.speed_dating.core.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

  public record ApiError(Instant timestamp, int status, String message) {
    static ApiError of(HttpStatus status, String message) {
      return new ApiError(Instant.now(), status.value(), message);
    }
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(ApiError.of(HttpStatus.FORBIDDEN, "Access denied"));
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .map(e -> e.getField() + ": " + e.getDefaultMessage())
      .collect(Collectors.joining("; "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ApiError.of(HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuth(AuthenticationException ex){
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(ApiError.of(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
  }
}
