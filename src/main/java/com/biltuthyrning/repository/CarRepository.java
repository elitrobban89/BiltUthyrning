package com.biltuthyrning.repository;

import com.biltuthyrning.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** @author Robert Andersson Kopler */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
}
