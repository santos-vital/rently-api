package com.rently.api.controller;

import com.rently.api.dto.customer.CustomerRequestDTO;
import com.rently.api.dto.customer.CustomerResponseDTO;
import com.rently.api.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(dto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  ResponseEntity<List<CustomerResponseDTO>> findAll() {
    return ResponseEntity.ok(customerService.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  ResponseEntity<CustomerResponseDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(customerService.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO dto) {
    return ResponseEntity.ok(customerService.update(id, dto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    customerService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  ResponseEntity<CustomerResponseDTO> getMe(Authentication authentication) {
    return ResponseEntity.ok(customerService.findByEmail(authentication.getName()));
  }

  @PutMapping("/me")
  ResponseEntity<CustomerResponseDTO> updateMe(
      @Valid @RequestBody CustomerRequestDTO dto,
      Authentication authentication) {
    return ResponseEntity.ok(customerService.updateByEmail(authentication.getName(), dto));
  }
}
