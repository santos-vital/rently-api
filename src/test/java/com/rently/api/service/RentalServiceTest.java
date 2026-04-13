package com.rently.api.service;

import com.rently.api.domain.Customer;
import com.rently.api.domain.Rental;
import com.rently.api.domain.RentalStatus;
import com.rently.api.domain.Role;
import com.rently.api.domain.User;
import com.rently.api.domain.Vehicle;
import com.rently.api.dto.rental.RentalRequestDTO;
import com.rently.api.dto.rental.RentalResponseDTO;
import com.rently.api.exception.BusinessException;
import com.rently.api.exception.ResourceNotFoundException;
import com.rently.api.repository.CustomerRepository;
import com.rently.api.repository.RentalRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

  @Mock private RentalRepository rentalRepository;
  @Mock private VehicleRepository vehicleRepository;
  @Mock private CustomerRepository customerRepository;

  @InjectMocks private RentalService rentalService;

  private Vehicle vehicle;
  private Customer customer;
  private User adminUser;
  private final LocalDate startDate = LocalDate.of(2026, 5, 1);
  private final LocalDate endDate = LocalDate.of(2026, 5, 4);

  @BeforeEach
  void setUp() {
    vehicle = Vehicle.builder()
        .id(1L).brand("Toyota").model("Corolla")
        .plate("ABC-1234").year(2022)
        .dailyRate(new BigDecimal("100.00"))
        .build();

    customer = Customer.builder()
        .id(1L).name("João Silva").cpf("123.456.789-00")
        .email("joao@email.com").phone("11999999999")
        .build();

    adminUser = User.builder()
        .id(1L).email("admin@rently.com")
        .name("Admin Rently").role(Role.ADMIN)
        .build();
  }

  // ─── Cálculo do valor total ───────────────────────────────────────────────

  @Test
  @DisplayName("Deve calcular o valor total corretamente com base na diária e no número de dias")
  void create_shouldCalculateTotalValueCorrectly() {
    var dto = new RentalRequestDTO(1L, 1L, startDate, endDate);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(rentalRepository.findConflictingRentals(any(), any(), any(), any())).thenReturn(List.of());
    when(rentalRepository.save(any())).thenAnswer(inv -> {
      Rental r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });

    RentalResponseDTO response = rentalService.create(dto, adminUser);

    // 3 dias × R$ 100,00 = R$ 300,00
    assertThat(response.totalValue()).isEqualByComparingTo(new BigDecimal("300.00"));
  }

  @Test
  @DisplayName("Deve calcular corretamente para diária com centavos")
  void create_shouldCalculateTotalValueWithDecimalDailyRate() {
    vehicle = Vehicle.builder()
        .id(1L).brand("Honda").model("Civic")
        .plate("XYZ-5678").year(2023)
        .dailyRate(new BigDecimal("150.50"))
        .build();

    var dto = new RentalRequestDTO(1L, 1L, startDate, endDate);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(rentalRepository.findConflictingRentals(any(), any(), any(), any())).thenReturn(List.of());
    when(rentalRepository.save(any())).thenAnswer(inv -> {
      Rental r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });

    RentalResponseDTO response = rentalService.create(dto, adminUser);

    // 3 dias × R$ 150,50 = R$ 451,50
    assertThat(response.totalValue()).isEqualByComparingTo(new BigDecimal("451.50"));
  }

  // ─── Validação de datas ───────────────────────────────────────────────────

  @Test
  @DisplayName("Deve lançar BusinessException quando endDate for igual a startDate")
  void create_shouldThrow_whenEndDateEqualsStartDate() {
    var dto = new RentalRequestDTO(1L, 1L, startDate, startDate);

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Data de término deve ser posterior à data de início");
  }

  @Test
  @DisplayName("Deve lançar BusinessException quando endDate for anterior a startDate")
  void create_shouldThrow_whenEndDateIsBeforeStartDate() {
    var dto = new RentalRequestDTO(1L, 1L, endDate, startDate);

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Data de término deve ser posterior à data de início");
  }

  // ─── Validação de disponibilidade ────────────────────────────────────────

  @Test
  @DisplayName("Deve lançar BusinessException quando veículo tiver conflito de datas")
  void create_shouldThrow_whenVehicleHasConflictingRental() {
    var dto = new RentalRequestDTO(1L, 1L, startDate, endDate);
    var existingRental = Rental.builder().id(99L).build();

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(rentalRepository.findConflictingRentals(any(), any(), any(), any()))
        .thenReturn(List.of(existingRental));

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Veículo não disponível no período selecionado");
  }

  @Test
  @DisplayName("Deve permitir aluguel de veículo cujo conflito foi cancelado")
  void create_shouldSucceed_whenConflictingRentalIsCancelled() {
    var dto = new RentalRequestDTO(1L, 1L, startDate, endDate);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    // Sem conflito com ACTIVE — aluguéis cancelados são ignorados pela query
    when(rentalRepository.findConflictingRentals(any(), any(), any(), any())).thenReturn(List.of());
    when(rentalRepository.save(any())).thenAnswer(inv -> {
      Rental r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });

    RentalResponseDTO response = rentalService.create(dto, adminUser);

    assertThat(response).isNotNull();
    assertThat(response.status()).isEqualTo(RentalStatus.ACTIVE);
  }

  // ─── Validação de entidades ───────────────────────────────────────────────

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando veículo não existir")
  void create_shouldThrow_whenVehicleNotFound() {
    // customerId=1L, vehicleId=99L → vehicle lookup falha
    var dto = new RentalRequestDTO(1L, 99L, startDate, endDate);

    when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException quando cliente não existir")
  void create_shouldThrow_whenCustomerNotFound() {
    // customerId=99L, vehicleId=1L → vehicle found, customer lookup falha
    var dto = new RentalRequestDTO(99L, 1L, startDate, endDate);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ─── Resolução de customerId ──────────────────────────────────────────────

  @Test
  @DisplayName("USER deve ter customerId derivado do token, ignorando o dto")
  void create_shouldUseTokenCustomerId_whenRoleIsUser() {
    User userRequester = User.builder()
        .id(2L).email("joao@email.com")
        .name("João Silva").role(Role.USER).customer(customer) // customer.id = 1L
        .build();

    // dto com customerId nulo — USER não precisa enviar
    var dto = new RentalRequestDTO(null, 1L, startDate, endDate);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(rentalRepository.findConflictingRentals(any(), any(), any(), any())).thenReturn(List.of());
    when(rentalRepository.save(any())).thenAnswer(inv -> {
      Rental r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });

    RentalResponseDTO response = rentalService.create(dto, userRequester);

    assertThat(response.customer().id()).isEqualTo(1L);
  }

  @Test
  @DisplayName("ADMIN deve lançar BusinessException quando customerId não for informado")
  void create_shouldThrow_whenAdminDoesNotProvideCustomerId() {
    var dto = new RentalRequestDTO(null, 1L, startDate, endDate);

    assertThatThrownBy(() -> rentalService.create(dto, adminUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage("customerId é obrigatório para administradores");
  }

  // ─── Cancelamento ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("Deve cancelar aluguel ativo com sucesso")
  void cancel_shouldCancelActiveRental() {
    var rental = Rental.builder()
        .id(1L).vehicle(vehicle).customer(customer)
        .startDate(startDate).endDate(endDate)
        .totalValue(new BigDecimal("300.00"))
        .status(RentalStatus.ACTIVE)
        .build();

    when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
    when(rentalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RentalResponseDTO response = rentalService.cancel(1L, adminUser);

    assertThat(response.status()).isEqualTo(RentalStatus.CANCELLED);
    verify(rentalRepository).save(rental);
  }

  @Test
  @DisplayName("Deve lançar BusinessException ao tentar cancelar aluguel já cancelado")
  void cancel_shouldThrow_whenRentalAlreadyCancelled() {
    var rental = Rental.builder()
        .id(1L).vehicle(vehicle).customer(customer)
        .startDate(startDate).endDate(endDate)
        .totalValue(new BigDecimal("300.00"))
        .status(RentalStatus.CANCELLED)
        .build();

    when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

    assertThatThrownBy(() -> rentalService.cancel(1L, adminUser))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Aluguel já está cancelado");
  }

  @Test
  @DisplayName("Deve lançar ResourceNotFoundException ao cancelar aluguel inexistente")
  void cancel_shouldThrow_whenRentalNotFound() {
    when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.cancel(99L, adminUser))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Deve lançar BusinessException quando USER tenta cancelar aluguel de outro cliente")
  void cancel_shouldThrow_whenUserCancelsOtherCustomerRental() {
    Customer otherCustomer = Customer.builder()
        .id(2L).name("Maria").cpf("999.999.999-99")
        .email("maria@email.com").phone("11988888888")
        .build();

    User userRequester = User.builder()
        .id(2L).email("joao@email.com")
        .role(Role.USER).customer(customer) // customer.id = 1L
        .build();

    var rental = Rental.builder()
        .id(1L).vehicle(vehicle).customer(otherCustomer) // pertence ao otherCustomer.id = 2L
        .startDate(startDate).endDate(endDate)
        .totalValue(new BigDecimal("300.00"))
        .status(RentalStatus.ACTIVE)
        .build();

    when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

    assertThatThrownBy(() -> rentalService.cancel(1L, userRequester))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Você não tem permissão para cancelar este aluguel");
  }
}
