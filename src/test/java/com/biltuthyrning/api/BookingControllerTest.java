package com.biltuthyrning.api;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerTest {

    private MockMvc mvc;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mvc = MockMvcBuilders.standaloneSetup(new BookingController(bookingService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    void batchTillganglighetReturnerarKartaPerBil() throws Exception {
        when(bookingService.availabilityForAll(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4)))
                .thenReturn(Map.of(1L, true, 2L, false));

        mvc.perform(get("/api/bookings/availability")
                        .param("start", "2026-07-01").param("end", "2026-07-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1").value(true))
                .andExpect(jsonPath("$.2").value(false));
    }

    @Test
    void perBilTillganglighetReturnerarStatus() throws Exception {
        when(bookingService.isCarAvailable(eq(5L), any(), any())).thenReturn(false);

        mvc.perform(get("/api/bookings/cars/5/availability")
                        .param("start", "2026-07-01").param("end", "2026-07-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.carId").value(5));
    }

    @Test
    void listarAllaBokningar() throws Exception {
        Car car = new Car("Volvo XC40", "2023", "B4 AWD", new BigDecimal("899.00"));
        car.setId(1L);
        Booking b = new Booking();
        b.setId(7L);
        b.setCar(car);
        b.setCustomerName("Anna");
        b.setStartDate(LocalDate.of(2026, 7, 1));
        b.setEndDate(LocalDate.of(2026, 7, 4));
        b.setTotalPrice(new BigDecimal("2697.00"));
        b.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingService.getAllBookings()).thenReturn(List.of(b));

        mvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].customerName").value("Anna"))
                .andExpect(jsonPath("$[0].car.model").value("Volvo XC40"));
    }
}
