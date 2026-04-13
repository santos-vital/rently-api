package com.rently.api.dto.vehicle;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VehicleRequestDTO(
    @NotBlank(message = "Marca é obrigatória") String brand,
    @NotBlank(message = "Modelo é obrigatório") String model,
    @NotBlank(message = "Placa é obrigatória") String plate,
    @NotNull(message = "Ano é obrigatório") @Min(value = 1900, message = "Ano inválido") Integer year,
    @NotNull(message = "Diária é obrigatória") @DecimalMin(value = "0.01", message = "Diária deve ser maior que zero") BigDecimal dailyRate
) {}
