package com.biltuthyrning.repository;

import com.biltuthyrning.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.status = 'CONFIRMED' " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(
        @Param("carId") Long carId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    List<Booking> findByCar_Id(Long carId);
}
