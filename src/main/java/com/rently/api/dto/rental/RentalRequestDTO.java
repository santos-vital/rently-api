package com.rently.api.dto.rental;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RentalRequestDTO(
    Long customerId,
    @NotNull(message = "Veículo é obrigatório") Long vehicleId,
    @NotNull(message = "Data de início é obrigatória") @FutureOrPresent(message = "Data de início não pode ser no passado") LocalDate startDate,
    @NotNull(message = "Data de término é obrigatória") LocalDate endDate
) {}
