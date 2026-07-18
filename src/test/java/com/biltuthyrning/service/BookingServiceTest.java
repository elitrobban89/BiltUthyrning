package com.biltuthyrning.service;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.repository.BookingRepository;
import com.biltuthyrning.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private BookingService bookingService;

    private Car car;

    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END   = LocalDate.of(2026, 7, 4); // 3 dagar

    @BeforeEach
    void setUp() {
        car = new Car("Volvo XC40", "2023", "B4 AWD", new BigDecimal("899.00"));
        car.setId(1L);
    }

    // ── isCarAvailable ────────────────────────────────────────────────────────

    @Test
    void isCarAvailable_returnTrue_whenNoConflicts() {
        when(bookingRepository.findConflictingBookings(1L, START, END))
            .thenReturn(Collections.emptyList());

        assertThat(bookingService.isCarAvailable(1L, START, END)).isTrue();
    }

    @Test
    void isCarAvailable_returnFalse_whenConflictExists() {
        when(bookingRepository.findConflictingBookings(1L, START, END))
            .thenReturn(List.of(new Booking()));

        assertThat(bookingService.isCarAvailable(1L, START, END)).isFalse();
    }

    // ── availabilityForAll (batch) ────────────────────────────────────────────

    @Test
    void availabilityForAll_marksOverlappingConfirmedAsBusy() {
        Car car2 = new Car("Volvo S90", "2023", "B5 AWD", new BigDecimal("1299.00"));
        car2.setId(2L);
        Booking confirmed = new Booking();
        confirmed.setCar(car);
        confirmed.setStartDate(START.plusDays(1));
        confirmed.setEndDate(END.plusDays(1));
        confirmed.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findAll()).thenReturn(List.of(confirmed));
        when(carRepository.findAll()).thenReturn(List.of(car, car2));

        var map = bookingService.availabilityForAll(START, END);

        assertThat(map).containsEntry(1L, false).containsEntry(2L, true);
    }

    @Test
    void availabilityForAll_ignoresCancelledAndNonOverlapping() {
        Booking cancelled = new Booking();
        cancelled.setCar(car);
        cancelled.setStartDate(START);
        cancelled.setEndDate(END);
        cancelled.setStatus(Booking.BookingStatus.CANCELLED);
        Booking farAway = new Booking();
        farAway.setCar(car);
        farAway.setStartDate(END.plusDays(10));
        farAway.setEndDate(END.plusDays(12));
        farAway.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findAll()).thenReturn(List.of(cancelled, farAway));
        when(carRepository.findAll()).thenReturn(List.of(car));

        assertThat(bookingService.availabilityForAll(START, END)).containsEntry(1L, true);
    }

    // ── calculateTotalPrice ───────────────────────────────────────────────────

    @Test
    void calculateTotalPrice_returnsCorrectAmount() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        BigDecimal price = bookingService.calculateTotalPrice(1L, START, END);

        // 3 dagar × 899 kr = 2 697 kr
        assertThat(price).isEqualByComparingTo(new BigDecimal("2697.00"));
    }

    @Test
    void calculateTotalPrice_treatsZeroDaysAsOneDay() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        // samma start och slut = 0 dagar → räknas som 1
        BigDecimal price = bookingService.calculateTotalPrice(1L, START, START);

        assertThat(price).isEqualByComparingTo(new BigDecimal("899.00"));
    }

    @Test
    void calculateTotalPrice_throwsWhenCarNotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.calculateTotalPrice(99L, START, END))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    // ── createBooking ─────────────────────────────────────────────────────────

    @Test
    void createBooking_savesConfirmedBookingWithCorrectFields() {
        when(bookingRepository.findConflictingBookings(1L, START, END))
            .thenReturn(Collections.emptyList());
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.createBooking(1L, "Anna Svensson", START, END);

        assertThat(result.getCar()).isEqualTo(car);
        assertThat(result.getCustomerName()).isEqualTo("Anna Svensson");
        assertThat(result.getStartDate()).isEqualTo(START);
        assertThat(result.getEndDate()).isEqualTo(END);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("2697.00"));
        assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
    }

    @Test
    void createBooking_throwsWhenCarNotAvailable() {
        when(bookingRepository.findConflictingBookings(1L, START, END))
            .thenReturn(List.of(new Booking()));

        assertThatThrownBy(() -> bookingService.createBooking(1L, "Anna Svensson", START, END))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not available");
    }

    @Test
    void createBooking_throwsWhenCarNotFound() {
        when(bookingRepository.findConflictingBookings(1L, START, END))
            .thenReturn(Collections.emptyList());
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(1L, "Anna Svensson", START, END))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Car not found");
    }

    // ── cancelBooking ─────────────────────────────────────────────────────────

    @Test
    void cancelBooking_setsStatusToCancelled() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_throwsWhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    @Test
    void cancelBooking_throwsWhenAlreadyCancelled() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("CONFIRMED");
    }

    @Test
    void cancelBooking_throwsWhenAlreadyCompleted() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
            .isInstanceOf(IllegalStateException.class);
    }

    // ── completeBooking ───────────────────────────────────────────────────────

    @Test
    void completeBooking_setsStatusToCompleted() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.completeBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.COMPLETED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBooking_throwsWhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.completeBooking(99L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeBooking_throwsWhenAlreadyCancelled() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.completeBooking(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("CONFIRMED");
    }

    @Test
    void completeBooking_throwsWhenAlreadyCompleted() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.completeBooking(1L))
            .isInstanceOf(IllegalStateException.class);
    }

    // ── getAllBookings ─────────────────────────────────────────────────────────

    @Test
    void getAllBookings_returnWhatRepositoryReturns() {
        List<Booking> expected = List.of(new Booking(), new Booking());
        when(bookingRepository.findAll()).thenReturn(expected);

        assertThat(bookingService.getAllBookings()).isEqualTo(expected);
    }

    // ── getBookingById ────────────────────────────────────────────────────────

    @Test
    void getBookingById_returnsBookingWhenFound() {
        Booking booking = bookingWithStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.getBookingById(1L)).isEqualTo(booking);
    }

    @Test
    void getBookingById_throwsWhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    // ── Hjälpmetoder ─────────────────────────────────────────────────────────

    private Booking bookingWithStatus(Booking.BookingStatus status) {
        Booking b = new Booking();
        b.setId(1L);
        b.setCar(car);
        b.setStatus(status);
        return b;
    }
}
