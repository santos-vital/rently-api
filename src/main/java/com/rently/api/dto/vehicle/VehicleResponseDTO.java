package com.rently.api.dto.vehicle;

import java.math.BigDecimal;

public record VehicleResponseDTO(
    Long id,
    String brand,
    String model,
    String plate,
    Integer year,
    BigDecimal dailyRate
) {}
