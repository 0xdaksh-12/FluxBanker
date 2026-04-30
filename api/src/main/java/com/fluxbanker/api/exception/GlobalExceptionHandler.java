package com.fluxbanker.api.exception;

import com.fluxbanker.api.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler.
 * All responses follow the { success, message, errors? } contract.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
        ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
        ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .build());
  }

  /**
   * Handles @Valid failures — mirrors Node's validationHandler Zod error mapping.
   * Returns only the first error per field (de-duplicated by path).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> ErrorResponse.FieldError.builder()
            .path(fe.getField())
            .message(fe.getDefaultMessage())
            .build())
        .toList();

    return ResponseEntity.badRequest().body(
        ErrorResponse.builder()
            .success(false)
            .message("Request validation failed")
            .errors(fieldErrors)
            .build());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        ErrorResponse.builder()
            .success(false)
            .message("Access denied")
            .build());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unhandled exception: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        ErrorResponse.builder()
            .success(false)
            .message("Something went wrong")
            .build());
  }
}
