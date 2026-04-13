package com.rently.api.dto.rental;

import com.rently.api.domain.RentalStatus;
import com.rently.api.dto.customer.CustomerResponseDTO;
import com.rently.api.dto.vehicle.VehicleResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalResponseDTO(
    Long id,
    VehicleResponseDTO vehicle,
    CustomerResponseDTO customer,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalValue,
    RentalStatus status
) {}
