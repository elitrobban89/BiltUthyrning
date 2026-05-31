package com.biltuthyrning;

import com.biltuthyrning.model.Car;
import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.User;
import com.biltuthyrning.repository.CarRepository;
import com.biltuthyrning.repository.BookingRepository;
import com.biltuthyrning.repository.UserRepository;
import com.biltuthyrning.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(CarRepository carRepository, BookingRepository bookingRepository,
                                            UserRepository userRepository, UserService userService) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(userService.hashPassword("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            if (carRepository.count() == 0) {
                Car car1 = Car.builder()
                    .model("Volvo XC40")
                    .year("2023")
                    .engine("B4 AWD")
                    .dailyRate(new BigDecimal("899.00"))
                    .build();

                Car car2 = Car.builder()
                    .model("Volvo S90")
                    .year("2023")
                    .engine("B5 AWD")
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
                    .dailyRate(new BigDecimal("1499.00"))
                    .build();

                Car car5 = Car.builder()
                    .model("Volvo EX60 P6")
                    .year("2025")
                    .engine("El — 300 kW")
                    .dailyRate(new BigDecimal("1199.00"))
                    .build();

                Car car6 = Car.builder()
                    .model("Volvo XC60 B4")
                    .year("2024")
                    .engine("2.0L B4 Mild Hybrid")
                    .dailyRate(new BigDecimal("999.00"))
                    .build();

                Car car7 = Car.builder()
                    .model("Volvo XC60 D5")
                    .year("2022")
                    .engine("2.0L D5 Diesel")
                    .dailyRate(new BigDecimal("949.00"))
                    .build();

                carRepository.save(car1);
                carRepository.save(car2);
                carRepository.save(car3);
                carRepository.save(car4);
                carRepository.save(car5);
                carRepository.save(car6);
                carRepository.save(car7);
                
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
