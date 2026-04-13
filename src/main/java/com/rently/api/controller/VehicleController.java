package com.rently.api.controller;

import com.rently.api.dto.vehicle.VehicleRequestDTO;
import com.rently.api.dto.vehicle.VehicleResponseDTO;
import com.rently.api.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(dto));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping
  ResponseEntity<List<VehicleResponseDTO>> findAll() {
    return ResponseEntity.ok(vehicleService.findAll());
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping("/available")
  ResponseEntity<List<VehicleResponseDTO>> findAvailable(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ResponseEntity.ok(vehicleService.findAvailable(startDate, endDate));
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping("/{id}")
  ResponseEntity<VehicleResponseDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleService.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  ResponseEntity<VehicleResponseDTO> update(@PathVariable Long id, @Valid @RequestBody VehicleRequestDTO dto) {
    return ResponseEntity.ok(vehicleService.update(id, dto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    vehicleService.delete(id);
    return ResponseEntity.noContent().build();
  }
}