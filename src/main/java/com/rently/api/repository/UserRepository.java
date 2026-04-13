package com.rently.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rently.api.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
