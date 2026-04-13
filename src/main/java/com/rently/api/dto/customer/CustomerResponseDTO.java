package com.rently.api.dto.customer;

public record CustomerResponseDTO(
    Long id,
    String name,
    String cpf,
    String email,
    String phone
) {}
