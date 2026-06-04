package com.biltuthyrning.repository;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Integrationstester för findConflictingBookings.
 *
 * @DataJpaTest startar bara JPA-lagret (inga controllers, services, GUI).
 * @AutoConfigureTestDatabase(replace = NONE) låter oss använda SQLite
 * in-memory istället för H2 som Spring annars väljer automatiskt.
 * Varje test körs i en transaktion som rullas tillbaka efteråt — databasen
 * är alltså tom i början av varje test.
 *
 * Befintlig bokning i alla tester: XC40, 5–10 juli (CONFIRMED).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    CarRepository carRepository;

    private Car car;

    private static final LocalDate JUL_05 = LocalDate.of(2026, 7, 5);
    private static final LocalDate JUL_10 = LocalDate.of(2026, 7, 10);

    @BeforeEach
    void setUp() {
        car = carRepository.save(new Car("Volvo XC40", "2023", "B4 AWD", new BigDecimal("899.00")));
        bookingRepository.save(confirmedBooking(car, JUL_05, JUL_10));
    }

    // ── Ingen konflikt ────────────────────────────────────────────────────────

    @Test
    void noConflict_whenNewBookingEndsBefore() {
        // Juli 1–4, slutar dagen innan befintlig börjar
        var result = query(car, date(7, 1), date(7, 4));

        assertThat(result).isEmpty();
    }

    @Test
    void noConflict_whenNewBookingStartsAfter() {
        // Juli 11–15, börjar dagen efter befintlig slutar
        var result = query(car, date(7, 11), date(7, 15));

        assertThat(result).isEmpty();
    }

    @Test
    void noConflict_whenDifferentCar() {
        Car otherCar = carRepository.save(new Car("Volvo S90", "2023", "B5 AWD", new BigDecimal("1299.00")));

        var result = query(otherCar, JUL_05, JUL_10);

        assertThat(result).isEmpty();
    }

    @Test
    void noConflict_whenCancelledBookingExistsForSameDates() {
        // Lägg till en CANCELLED bokning för samma period — ska inte räknas
        Booking cancelled = confirmedBooking(car, JUL_05, JUL_10);
        cancelled.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(cancelled);

        var result = query(car, JUL_05, JUL_10);

        // Bara den CONFIRMED bokningen ska returneras, inte den CANCELLED
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
    }

    // ── Konflikt ──────────────────────────────────────────────────────────────

    @Test
    void conflict_whenNewBookingOverlapsStart() {
        // Juli 3–7, överlappar i början
        var result = query(car, date(7, 3), date(7, 7));

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenNewBookingOverlapsEnd() {
        // Juli 8–12, överlappar i slutet
        var result = query(car, date(7, 8), date(7, 12));

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenNewBookingContainsExisting() {
        // Juli 3–15, omsluter hela den befintliga bokningen
        var result = query(car, date(7, 3), date(7, 15));

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenNewBookingIsInsideExisting() {
        // Juli 6–8, ligger helt inuti den befintliga
        var result = query(car, date(7, 6), date(7, 8));

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenNewBookingEndsOnStartDateOfExisting() {
        // Juli 1–5: slutdagen sammanfaller med befintligas startdag
        // Nuvarande query räknar detta som konflikt (<=/>= på gränsen)
        var result = query(car, date(7, 1), JUL_05);

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenNewBookingStartsOnEndDateOfExisting() {
        // Juli 10–15: startdagen sammanfaller med befintligas slutdag
        var result = query(car, JUL_10, date(7, 15));

        assertThat(result).hasSize(1);
    }

    @Test
    void conflict_whenExactSameDates() {
        var result = query(car, JUL_05, JUL_10);

        assertThat(result).hasSize(1);
    }

    // ── Hjälpmetoder ─────────────────────────────────────────────────────────

    private List<Booking> query(Car c, LocalDate start, LocalDate end) {
        return bookingRepository.findConflictingBookings(c.getId(), start, end);
    }

    private static LocalDate date(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private Booking confirmedBooking(Car c, LocalDate start, LocalDate end) {
        Booking b = new Booking();
        b.setCar(c);
        b.setCustomerName("Test Kund");
        b.setStartDate(start);
        b.setEndDate(end);
        b.setTotalPrice(new BigDecimal("4485.00"));
        b.setStatus(Booking.BookingStatus.CONFIRMED);
        return b;
    }
}
