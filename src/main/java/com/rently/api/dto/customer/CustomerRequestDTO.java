package com.rently.api.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequestDTO(
    @NotBlank(message = "Nome é obrigatório") String name,
    @NotBlank(message = "CPF é obrigatório") String cpf,
    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
    @NotBlank(message = "Telefone é obrigatório") String phone
) {}
