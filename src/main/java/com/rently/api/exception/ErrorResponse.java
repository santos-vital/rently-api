package com.rently.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<FieldError> errors
) {

  public record FieldError(String field, String message) {}

  public static ErrorResponse of(int status, String error, String message, String path) {
    return new ErrorResponse(status, error, message, path, LocalDateTime.now(), null);
  }

  public static ErrorResponse of(int status, String error, String message, String path, List<FieldError> errors) {
    return new ErrorResponse(status, error, message, path, LocalDateTime.now(), errors);
  }
}