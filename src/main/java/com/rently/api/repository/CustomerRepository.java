package com.rently.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rently.api.domain.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findByEmail(String email);

  Optional<Customer> findByCpf(String cpf);
}
