package com.rently.api.service;

import com.rently.api.domain.Customer;
import com.rently.api.domain.Role;
import com.rently.api.domain.User;
import com.rently.api.dto.auth.AuthRequestDTO;
import com.rently.api.dto.auth.AuthResponseDTO;
import com.rently.api.dto.auth.RegisterRequestDTO;
import com.rently.api.exception.BusinessException;
import com.rently.api.exception.EmailAlreadyExistsException;
import com.rently.api.repository.CustomerRepository;
import com.rently.api.repository.UserRepository;
import com.rently.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponseDTO register(RegisterRequestDTO request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new EmailAlreadyExistsException();
    }

    Role role = request.role() != null ? request.role() : Role.USER;

    if (request.name() == null || request.name().isBlank()) {
      throw new BusinessException("Nome é obrigatório");
    }

    if (role == Role.USER) {
      if (request.cpf() == null || request.cpf().isBlank()) {
        throw new BusinessException("CPF é obrigatório para clientes");
      }
      if (request.phone() == null || request.phone().isBlank()) {
        throw new BusinessException("Telefone é obrigatório para clientes");
      }
    }

    var user = User.builder()
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(role)
        .name(request.name())
        .build();

    if (role == Role.USER) {
      var customer = customerRepository.save(Customer.builder()
          .name(request.name())
          .cpf(request.cpf())
          .email(request.email())
          .phone(request.phone())
          .build());
      user.setCustomer(customer);
    }

    userRepository.save(user);
    return new AuthResponseDTO(jwtService.generateToken(user));
  }

  public AuthResponseDTO login(AuthRequestDTO request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );
    var user = userRepository.findByEmail(request.email()).orElseThrow();
    return new AuthResponseDTO(jwtService.generateToken(user));
  }
}
