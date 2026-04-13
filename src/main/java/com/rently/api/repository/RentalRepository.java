package com.rently.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rently.api.domain.Rental;
import com.rently.api.domain.RentalStatus;

public interface RentalRepository extends JpaRepository<Rental, Long> {

  @Query("""
      SELECT r FROM Rental r
      WHERE r.vehicle.id = :vehicleId
      AND r.status = :status
      AND r.startDate < :endDate
      AND r.endDate > :startDate
      """)
  List<Rental> findConflictingRentals(
      @Param("vehicleId") Long vehicleId,
      @Param("status") RentalStatus status,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  List<Rental> findByCustomer_Id(Long customerId);
}
