package com.rently.api.service;

import com.rently.api.domain.RentalStatus;
import com.rently.api.domain.Vehicle;
import com.rently.api.dto.vehicle.VehicleResponseDTO;
import com.rently.api.exception.BusinessException;
import com.rently.api.exception.ResourceNotFoundException;
import com.rently.api.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private VehicleService vehicleService;

  private Vehicle vehicle;
  private final LocalDate startDate = LocalDate.of(2026, 5, 1);
  private final LocalDate endDate = LocalDate.of(2026, 5, 4);

  @BeforeEach
  void setUp() {
    vehicle = Vehicle.builder()
        .id(1L).brand("Toyota").model("Corolla")
        .plate("ABC-1234").year(2022)
        .dailyRate(new BigDecimal("100.00"))
        .build();
  }

  // ─── findAvailable ────────────────────────────────────────────────────────

  @Test
  @DisplayName("Deve retornar veículos disponíveis no período informado")
  void findAvailable_shouldReturnAvailableVehicles() {
    when(vehicleRepository.findAvailableVehicles(RentalStatus.ACTIVE, startDate, endDate))
        .thenReturn(List.of(vehicle));

    List<VehicleResponseDTO> result = vehicleService.findAvailable(startDate, endDate);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).plate()).isEqualTo("ABC-1234");
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não houver veículos disponíveis")
  void findAvailable_shouldReturnEmptyList_whenNoVehiclesAvailable() {
    when(vehicleRepository.findAvailableVehicles(RentalStatus.ACTIVE, startDate, endDate))
        .thenReturn(List.of());

    List<VehicleResponseDTO> result = vehicleService.findAvailable(startDate, endDate);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve lançar BusinessException quando endDate for igual a startDate")
  void findAvailable_shouldThrow_whenEndDateEqualsStartDate() {
    assertThatThrownBy(() -> vehicleService.findAvailable(startDate, startDate))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Data de término deve ser posterior à data de início");
  }

  @Test
  @DisplayName("Deve lançar BusinessException quando endDate for anterior a startDate")
  void findAvailable_shouldThrow_whenEndDateIsBeforeStartDate() {
    assertThatThrownBy(() -> vehicleService.findAvailable(endDate, startDate))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Data de término deve ser posterior à data de início");
  }

  // ─── findById ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Deve retornar veículo quando encontrado pelo id")
  void findById_shouldReturnVehicle_whenFound() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    VehicleResponseDTO result = vehicleService.findById(1L);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.brand()).isEqualTo("Toyota");
    assertThat(result.plate()).isEqualTo("ABC-1234");
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando veículo não existir")
  void findById_shouldThrow_whenNotFound() {
    when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ─── delete ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException ao deletar veículo inexistente")
  void delete_shouldThrow_whenNotFound() {
    when(vehicleRepository.existsById(99L)).thenReturn(false);

    assertThatThrownBy(() -> vehicleService.delete(99L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(vehicleRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Deve deletar veículo quando encontrado")
  void delete_shouldDeleteVehicle_whenFound() {
    when(vehicleRepository.existsById(1L)).thenReturn(true);

    vehicleService.delete(1L);

    verify(vehicleRepository).deleteById(1L);
  }
}
