package com.rently.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, "Conflict", ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ErrorResponse.of(422, "Unprocessable Entity", ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> new ErrorResponse.FieldError(e.getField(), e.getDefaultMessage()))
        .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(400, "Bad Request", "Erro de validação", request.getRequestURI(), fieldErrors));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(403, "Forbidden", "Acesso negado", request.getRequestURI()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(401, "Unauthorized", "Email ou senha inválidos", request.getRequestURI()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(500, "Internal Server Error", "Erro interno do servidor", request.getRequestURI()));
  }
}