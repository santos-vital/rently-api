package com.rently.api.controller;

import com.rently.api.domain.User;
import com.rently.api.dto.rental.RentalRequestDTO;
import com.rently.api.dto.rental.RentalResponseDTO;
import com.rently.api.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

  private final RentalService rentalService;

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @PostMapping
  ResponseEntity<RentalResponseDTO> create(
      @Valid @RequestBody RentalRequestDTO dto,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.create(dto, user));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping
  ResponseEntity<List<RentalResponseDTO>> findAll(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(rentalService.findAll(user));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping("/{id}")
  ResponseEntity<RentalResponseDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(rentalService.findById(id));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @PatchMapping("/{id}/cancel")
  ResponseEntity<RentalResponseDTO> cancel(
      @PathVariable Long id,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(rentalService.cancel(id, user));
  }
}
