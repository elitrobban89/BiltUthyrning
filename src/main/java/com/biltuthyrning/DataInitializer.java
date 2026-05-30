package com.biltuthyrning;

import com.biltuthyrning.model.Car;
import com.biltuthyrning.model.Booking;
import com.biltuthyrning.repository.CarRepository;
import com.biltuthyrning.repository.BookingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initializeData(CarRepository carRepository, BookingRepository bookingRepository) {
        return args -> {
            if (carRepository.count() == 0) {
                Car car1 = Car.builder()
                    .model("Volvo XC40")
                    .year("2023")
                    .engine("2.0L Turbo")
                    .dailyRate(new BigDecimal("899.00"))
                    .build();
                
                Car car2 = Car.builder()
                    .model("Volvo S90")
                    .year("2023")
                    .engine("2.0L Turbo")
                    .dailyRate(new BigDecimal("1299.00"))
                    .build();
                
                Car car3 = Car.builder()
                    .model("Volvo V90")
                    .year("2022")
                    .engine("2.0L B4")
                    .dailyRate(new BigDecimal("1099.00"))
                    .build();

                Car car4 = Car.builder()
                        .model("Volvo XC90")
                        .year("2025")
                        .engine("1.5L B5")
                        .dailyRate(new BigDecimal("1299.00"))
                        .build();
                
                carRepository.save(car1);
                carRepository.save(car2);
                carRepository.save(car3);
                carRepository.save(car4);
                
                Booking booking1 = new Booking();
                booking1.setCar(car1);
                booking1.setCustomerName("John Doe");
                booking1.setStartDate(LocalDate.now().plusDays(1));
                booking1.setEndDate(LocalDate.now().plusDays(5));
                booking1.setTotalPrice(new BigDecimal("3596.00"));
                booking1.setStatus(Booking.BookingStatus.CONFIRMED);
                
                bookingRepository.save(booking1);
            }
        };
    }
}
