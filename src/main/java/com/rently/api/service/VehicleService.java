package com.rently.api.service;

import com.rently.api.config.CacheConfig;
import com.rently.api.domain.RentalStatus;
import com.rently.api.domain.Vehicle;
import com.rently.api.dto.vehicle.VehicleRequestDTO;
import com.rently.api.dto.vehicle.VehicleResponseDTO;
import com.rently.api.exception.BusinessException;
import com.rently.api.exception.ResourceNotFoundException;
import com.rently.api.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

  private final VehicleRepository vehicleRepository;

  @Caching(evict = {
      @CacheEvict(value = CacheConfig.VEHICLES, allEntries = true),
      @CacheEvict(value = CacheConfig.VEHICLES_AVAILABLE, allEntries = true)
  })
  public VehicleResponseDTO create(VehicleRequestDTO dto) {
    var vehicle = Vehicle.builder()
        .brand(dto.brand())
        .model(dto.model())
        .plate(dto.plate())
        .year(dto.year())
        .dailyRate(dto.dailyRate())
        .build();
    return toDTO(vehicleRepository.save(vehicle));
  }

  @Cacheable(CacheConfig.VEHICLES)
  public List<VehicleResponseDTO> findAll() {
    return vehicleRepository.findAll().stream().map(this::toDTO).toList();
  }

  @Cacheable(value = CacheConfig.VEHICLES_AVAILABLE, key = "#startDate + '-' + #endDate")
  public List<VehicleResponseDTO> findAvailable(LocalDate startDate, LocalDate endDate) {
    if (!endDate.isAfter(startDate)) {
      throw new BusinessException("Data de término deve ser posterior à data de início");
    }
    return vehicleRepository.findAvailableVehicles(RentalStatus.ACTIVE, startDate, endDate)
        .stream().map(this::toDTO).toList();
  }

  public VehicleResponseDTO findById(Long id) {
    return vehicleRepository.findById(id)
        .map(this::toDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Veículo com id " + id + " não encontrado"));
  }

  @Caching(evict = {
      @CacheEvict(value = CacheConfig.VEHICLES, allEntries = true),
      @CacheEvict(value = CacheConfig.VEHICLES_AVAILABLE, allEntries = true)
  })
  public VehicleResponseDTO update(Long id, VehicleRequestDTO dto) {
    var vehicle = vehicleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Veículo com id " + id + " não encontrado"));
    vehicle.setBrand(dto.brand());
    vehicle.setModel(dto.model());
    vehicle.setPlate(dto.plate());
    vehicle.setYear(dto.year());
    vehicle.setDailyRate(dto.dailyRate());
    return toDTO(vehicleRepository.save(vehicle));
  }

  @Caching(evict = {
      @CacheEvict(value = CacheConfig.VEHICLES, allEntries = true),
      @CacheEvict(value = CacheConfig.VEHICLES_AVAILABLE, allEntries = true)
  })
  public void delete(Long id) {
    if (!vehicleRepository.existsById(id)) {
      throw new ResourceNotFoundException("Veículo com id " + id + " não encontrado");
    }
    vehicleRepository.deleteById(id);
  }

  private VehicleResponseDTO toDTO(Vehicle vehicle) {
    return new VehicleResponseDTO(
        vehicle.getId(),
        vehicle.getBrand(),
        vehicle.getModel(),
        vehicle.getPlate(),
        vehicle.getYear(),
        vehicle.getDailyRate()
    );
  }
}
