package com.biltuthyrning.api;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }
    
    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(
            request.getCarId(),
            request.getCustomerName(),
            request.getStartDate(),
            request.getEndDate()
        );
    }

    @DeleteMapping("/{id}")
    public void cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }
    
    @GetMapping("/availability")
    public Map<Long, Boolean> checkAllAvailability(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return bookingService.availabilityForAll(start, end);
    }

    @GetMapping("/cars/{carId}/availability")
    public Map<String, Object> checkAvailability(
        @PathVariable Long carId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        boolean available = bookingService.isCarAvailable(carId, start, end);
        return Map.of(
            "carId", carId,
            "startDate", start,
            "endDate", end,
            "available", available
        );
    }
}
