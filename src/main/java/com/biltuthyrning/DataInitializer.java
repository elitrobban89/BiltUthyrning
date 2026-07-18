package com.biltuthyrning;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.model.User;
import com.biltuthyrning.repository.BookingRepository;
import com.biltuthyrning.repository.CarRepository;
import com.biltuthyrning.repository.UserRepository;
import com.biltuthyrning.service.FleetSyncService;
import com.biltuthyrning.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    /** Målstorlek för flottan — basflottan (10 Volvo) fylls upp med bilar ur den delade databasen. */
    static final int FLEET_TARGET = 100;

    @Bean
    public CommandLineRunner initializeData(CarRepository carRepository, BookingRepository bookingRepository,
                                            UserRepository userRepository, UserService userService,
                                            FleetSyncService fleetSyncService) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(userService.hashPassword("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            Set<String> existingModels = carRepository.findAll().stream()
                .map(Car::getModel)
                .collect(Collectors.toSet());

            List<Car> fleet = List.of(
                Car.builder().model("Volvo XC40").year("2023").engine("B4 AWD").dailyRate(new BigDecimal("899.00")).build(),
                Car.builder().model("Volvo S90").year("2023").engine("B5 AWD").dailyRate(new BigDecimal("1299.00")).build(),
                Car.builder().model("Volvo V90").year("2022").engine("2.0L B4").dailyRate(new BigDecimal("1099.00")).build(),
                Car.builder().model("Volvo XC90").year("2025").engine("1.5L B5").dailyRate(new BigDecimal("1499.00")).build(),
                Car.builder().model("Volvo EX60 P6").year("2025").engine("El — 300 kW").dailyRate(new BigDecimal("1199.00")).build(),
                Car.builder().model("Volvo XC60 B4").year("2024").engine("2.0L B4 Mild Hybrid").dailyRate(new BigDecimal("999.00")).build(),
                Car.builder().model("Volvo XC60 D5").year("2022").engine("2.0L D5 Diesel").dailyRate(new BigDecimal("949.00")).build(),
                Car.builder().model("Volvo EX30 Twin Motor").year("2024").engine("El — 315 kW Twin Motor").dailyRate(new BigDecimal("1099.00")).build(),
                Car.builder().model("Volvo V60 B6").year("2023").engine("3.0L B6 Bensin + dragkrok").dailyRate(new BigDecimal("1199.00")).build(),
                Car.builder().model("Volvo XC60 T8 Recharge").year("2024").engine("T8 Laddhybrid — 340 kW").dailyRate(new BigDecimal("1349.00")).build()
            );

            fleet.stream()
                .filter(c -> !existingModels.contains(c.getModel()))
                .forEach(carRepository::save);

            long count = carRepository.count();
            if (count < FLEET_TARGET) {
                List<String> existing = carRepository.findAll().stream()
                    .map(c -> c.getModel().toLowerCase())
                    .toList();
                fleetSyncService.fetchSharedFleet(FLEET_TARGET - (int) count).stream()
                    .filter(c -> existing.stream().noneMatch(m ->
                        m.contains(c.getModel().toLowerCase()) || c.getModel().toLowerCase().contains(m)))
                    .forEach(carRepository::save);
            }

            if (bookingRepository.count() == 0) {
                Car xc40 = carRepository.findAll().stream()
                    .filter(c -> c.getModel().equals("Volvo XC40"))
                    .findFirst().orElse(null);
                if (xc40 != null) {
                    Booking booking1 = new Booking();
                    booking1.setCar(xc40);
                    booking1.setCustomerName("John Doe");
                    booking1.setStartDate(LocalDate.now().plusDays(1));
                    booking1.setEndDate(LocalDate.now().plusDays(5));
                    booking1.setTotalPrice(new BigDecimal("3596.00"));
                    booking1.setStatus(Booking.BookingStatus.CONFIRMED);
                    bookingRepository.save(booking1);
                }
            }
        };
    }
}
