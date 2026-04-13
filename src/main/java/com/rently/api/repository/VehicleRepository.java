package com.rently.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rently.api.domain.RentalStatus;
import com.rently.api.domain.Vehicle;

import java.time.LocalDate;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  @Query("""
      SELECT v FROM Vehicle v
      WHERE v.id NOT IN (
          SELECT r.vehicle.id FROM Rental r
          WHERE r.status = :status
          AND r.startDate < :endDate
          AND r.endDate > :startDate
      )
      """)
  List<Vehicle> findAvailableVehicles(
      @Param("status") RentalStatus status,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
