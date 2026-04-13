package com.rently.api.controller;

import com.rently.api.dto.auth.AuthRequestDTO;
import com.rently.api.dto.auth.AuthResponseDTO;
import com.rently.api.dto.auth.RegisterRequestDTO;
import com.rently.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  @PostMapping("/login")
  ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
    return ResponseEntity.ok(authService.login(request));
  }
}
