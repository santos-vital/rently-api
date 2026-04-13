package com.rently.api.service;

import com.rently.api.config.CacheConfig;
import com.rently.api.domain.Rental;
import com.rently.api.domain.RentalStatus;
import com.rently.api.domain.Role;
import com.rently.api.domain.User;
import com.rently.api.dto.customer.CustomerResponseDTO;
import com.rently.api.dto.rental.RentalRequestDTO;
import com.rently.api.dto.rental.RentalResponseDTO;
import com.rently.api.dto.vehicle.VehicleResponseDTO;
import com.rently.api.exception.BusinessException;
import com.rently.api.exception.ResourceNotFoundException;
import com.rently.api.repository.CustomerRepository;
import com.rently.api.repository.RentalRepository;
import com.rently.api.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {

  private final RentalRepository rentalRepository;
  private final VehicleRepository vehicleRepository;
  private final CustomerRepository customerRepository;

  @CacheEvict(value = CacheConfig.VEHICLES_AVAILABLE, allEntries = true)
  public RentalResponseDTO create(RentalRequestDTO dto, User requester) {
    if (!dto.endDate().isAfter(dto.startDate())) {
      throw new BusinessException("Data de término deve ser posterior à data de início");
    }

    Long customerId = resolveCustomerId(dto, requester);

    var vehicle = vehicleRepository.findById(dto.vehicleId())
        .orElseThrow(() -> new ResourceNotFoundException("Veículo com id " + dto.vehicleId() + " não encontrado"));

    var customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente com id " + customerId + " não encontrado"));

    var conflicts = rentalRepository.findConflictingRentals(
        dto.vehicleId(), RentalStatus.ACTIVE, dto.startDate(), dto.endDate()
    );
    if (!conflicts.isEmpty()) {
      throw new BusinessException("Veículo não disponível no período selecionado");
    }

    long days = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate());
    BigDecimal totalValue = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));

    var rental = Rental.builder()
        .vehicle(vehicle)
        .customer(customer)
        .startDate(dto.startDate())
        .endDate(dto.endDate())
        .totalValue(totalValue)
        .status(RentalStatus.ACTIVE)
        .build();

    return toDTO(rentalRepository.save(rental));
  }

  public List<RentalResponseDTO> findAll(User requester) {
    if (requester.getRole() == Role.ADMIN) {
      return rentalRepository.findAll().stream().map(this::toDTO).toList();
    }
    Long customerId = requester.getCustomer().getId();
    return rentalRepository.findByCustomer_Id(customerId).stream().map(this::toDTO).toList();
  }

  public RentalResponseDTO findById(Long id) {
    return rentalRepository.findById(id)
        .map(this::toDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Aluguel com id " + id + " não encontrado"));
  }

  @CacheEvict(value = CacheConfig.VEHICLES_AVAILABLE, allEntries = true)
  public RentalResponseDTO cancel(Long id, User requester) {
    var rental = rentalRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Aluguel com id " + id + " não encontrado"));
    if (rental.getStatus() == RentalStatus.CANCELLED) {
      throw new BusinessException("Aluguel já está cancelado");
    }
    if (requester.getRole() == Role.USER) {
      Long customerId = requester.getCustomer().getId();
      if (!rental.getCustomer().getId().equals(customerId)) {
        throw new BusinessException("Você não tem permissão para cancelar este aluguel");
      }
    }
    rental.setStatus(RentalStatus.CANCELLED);
    return toDTO(rentalRepository.save(rental));
  }

  private Long resolveCustomerId(RentalRequestDTO dto, User requester) {
    if (requester.getRole() == Role.USER) {
      return requester.getCustomer().getId();
    }
    if (dto.customerId() == null) {
      throw new BusinessException("customerId é obrigatório para administradores");
    }
    return dto.customerId();
  }

  private RentalResponseDTO toDTO(Rental rental) {
    var vehicle = rental.getVehicle();
    var customer = rental.getCustomer();

    var vehicleDTO = new VehicleResponseDTO(
        vehicle.getId(), vehicle.getBrand(), vehicle.getModel(),
        vehicle.getPlate(), vehicle.getYear(), vehicle.getDailyRate()
    );
    var customerDTO = new CustomerResponseDTO(
        customer.getId(), customer.getName(), customer.getCpf(),
        customer.getEmail(), customer.getPhone()
    );

    return new RentalResponseDTO(
        rental.getId(), vehicleDTO, customerDTO,
        rental.getStartDate(), rental.getEndDate(),
        rental.getTotalValue(), rental.getStatus()
    );
  }
}
