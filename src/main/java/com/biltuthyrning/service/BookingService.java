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

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }
}

