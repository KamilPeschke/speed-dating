package com.pairs.speed_dating.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponseHandler> handleUserNotFoundException(UserNotFoundException e) {
    log.warn("User not found exception: {}", e.getMessage());

    ErrorResponseHandler error = new ErrorResponseHandler(e.getMessage(), HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseHandler> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    log.warn("User already exists exception: {}", e.getMessage());

    ErrorResponseHandler error = new ErrorResponseHandler(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
