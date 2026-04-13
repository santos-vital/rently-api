package com.rently.api.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String brand;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false, unique = true)
  private String plate;

  @Column(nullable = false, name = "manufacture_year")
  private Integer year;

  @Column(nullable = false)
  private BigDecimal dailyRate;
  
}
