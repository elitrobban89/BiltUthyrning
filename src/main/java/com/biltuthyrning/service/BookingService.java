package com.biltuthyrning.service;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.repository.BookingRepository;
import com.biltuthyrning.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;

    public BookingService(BookingRepository bookingRepository, CarRepository carRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
    }
    
    public boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(carId, startDate, endDate);
        return conflicts.isEmpty();
    }

    /**
     * Tillgänglighet för HELA flottan i ett svar — dashboarden gjorde tidigare ett
     * HTTP-anrop per bil, ohållbart med ~100 bilar mot en pool på 3 anslutningar.
     * Samma överlappsregel som findConflictingBookings: CONFIRMED och start <= end.
     */
    public Map<Long, Boolean> availabilityForAll(LocalDate startDate, LocalDate endDate) {
        Set<Long> busy = bookingRepository.findAll().stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
            .filter(b -> !b.getStartDate().isAfter(endDate) && !b.getEndDate().isBefore(startDate))
            .map(b -> b.getCar().getId())
            .collect(Collectors.toSet());
        return carRepository.findAll().stream()
            .collect(Collectors.toMap(Car::getId, c -> !busy.contains(c.getId())));
    }
    
    public BigDecimal calculateTotalPrice(Long carId, LocalDate startDate, LocalDate endDate) {
        Car car = carRepository.findById(carId)
            .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;
        
        return car.getDailyRate().multiply(BigDecimal.valueOf(days));
    }
    
    public Booking createBooking(Long carId, String customerName, LocalDate startDate, 
                                 LocalDate endDate) {
        if (!isCarAvailable(carId, startDate, endDate)) {
            throw new IllegalArgumentException("Car is not available for the selected dates");
        }
        
        Car car = carRepository.findById(carId)
            .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        
        BigDecimal totalPrice = calculateTotalPrice(carId, startDate, endDate);
        
        Booking booking = new Booking();
        booking.setCar(car);
        booking.setCustomerName(customerName);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        
        return bookingRepository.save(booking);
    }
    
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be cancelled");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be marked as completed");
        }
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    public void deleteAllBookings() {
        bookingRepository.deleteAll();
    }
}

