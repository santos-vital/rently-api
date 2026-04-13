package com.rently.api.service;

import com.rently.api.config.CacheConfig;
import com.rently.api.domain.Customer;
import com.rently.api.dto.customer.CustomerRequestDTO;
import com.rently.api.dto.customer.CustomerResponseDTO;
import com.rently.api.exception.ConflictException;
import com.rently.api.exception.EmailAlreadyExistsException;
import com.rently.api.exception.ResourceNotFoundException;
import com.rently.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;

  @CacheEvict(value = CacheConfig.CUSTOMERS, allEntries = true)
  public CustomerResponseDTO create(CustomerRequestDTO dto) {
    if (customerRepository.findByEmail(dto.email()).isPresent()) {
      throw new EmailAlreadyExistsException();
    }
    if (customerRepository.findByCpf(dto.cpf()).isPresent()) {
      throw new ConflictException("CPF já cadastrado");
    }
    var customer = Customer.builder()
        .name(dto.name())
        .cpf(dto.cpf())
        .email(dto.email())
        .phone(dto.phone())
        .build();
    return toDTO(customerRepository.save(customer));
  }

  @Cacheable(CacheConfig.CUSTOMERS)
  public List<CustomerResponseDTO> findAll() {
    return customerRepository.findAll().stream().map(this::toDTO).toList();
  }

  public CustomerResponseDTO findById(Long id) {
    return customerRepository.findById(id)
        .map(this::toDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente com id " + id + " não encontrado"));
  }

  @CacheEvict(value = CacheConfig.CUSTOMERS, allEntries = true)
  public CustomerResponseDTO update(Long id, CustomerRequestDTO dto) {
    var customer = customerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente com id " + id + " não encontrado"));
    customerRepository.findByEmail(dto.email())
        .filter(c -> !c.getId().equals(id))
        .ifPresent(c -> { throw new EmailAlreadyExistsException(); });
    customerRepository.findByCpf(dto.cpf())
        .filter(c -> !c.getId().equals(id))
        .ifPresent(c -> { throw new ConflictException("CPF já cadastrado"); });
    customer.setName(dto.name());
    customer.setCpf(dto.cpf());
    customer.setEmail(dto.email());
    customer.setPhone(dto.phone());
    return toDTO(customerRepository.save(customer));
  }

  public CustomerResponseDTO findByEmail(String email) {
    return customerRepository.findByEmail(email)
        .map(this::toDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente com email " + email + " não encontrado"));
  }

  @CacheEvict(value = CacheConfig.CUSTOMERS, allEntries = true)
  public CustomerResponseDTO updateByEmail(String email, CustomerRequestDTO dto) {
    var customer = customerRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente com email " + email + " não encontrado"));
    customer.setName(dto.name());
    customer.setCpf(dto.cpf());
    customer.setPhone(dto.phone());
    return toDTO(customerRepository.save(customer));
  }

  @CacheEvict(value = CacheConfig.CUSTOMERS, allEntries = true)
  public void delete(Long id) {
    if (!customerRepository.existsById(id)) {
      throw new ResourceNotFoundException("Cliente com id " + id + " não encontrado");
    }
    customerRepository.deleteById(id);
  }

  private CustomerResponseDTO toDTO(Customer customer) {
    return new CustomerResponseDTO(
        customer.getId(),
        customer.getName(),
        customer.getCpf(),
        customer.getEmail(),
        customer.getPhone()
    );
  }
}
