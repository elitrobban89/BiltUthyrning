package com.biltuthyrning.web;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.CarService;
import com.biltuthyrning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebControllerTest {

    private MockMvc mvc;
    private CarService carService;
    private BookingService bookingService;
    private UserService userService;

    /** Standalone MockMvc saknar Spring Security — resolvern matar @AuthenticationPrincipal. */
    private static final HandlerMethodArgumentResolver PRINCIPAL = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return UserDetails.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return User.withUsername("admin").password("x").roles("ADMIN").build();
        }
    };

    @BeforeEach
    void setUp() {
        carService = mock(CarService.class);
        bookingService = mock(BookingService.class);
        userService = mock(UserService.class);
        InternalResourceViewResolver views = new InternalResourceViewResolver("/templates/", ".html");
        mvc = MockMvcBuilders.standaloneSetup(new WebController(carService, bookingService, userService))
                .setCustomArgumentResolvers(PRINCIPAL)
                .setViewResolvers(views)
                .build();
    }

    @Test
    void dashboardRaknarAktivaBokningarOchIntaktUtanAvbokade() throws Exception {
        Car car = new Car("Volvo XC40", "2023", "B4 AWD", new BigDecimal("899.00"));
        car.setId(1L);
        Booking confirmed = booking(car, Booking.BookingStatus.CONFIRMED, "2697.00");
        Booking completed = booking(car, Booking.BookingStatus.COMPLETED, "1798.00");
        Booking cancelled = booking(car, Booking.BookingStatus.CANCELLED, "899.00");
        when(carService.getAllCars()).thenReturn(List.of(car));
        when(bookingService.getAllBookings()).thenReturn(List.of(confirmed, completed, cancelled));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("activeBookings", 1L))
                .andExpect(model().attribute("totalRevenue", 4495))
                .andExpect(model().attribute("username", "admin"));
    }

    @Test
    void healthSvararOk() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void lyckadBokningGerSuccessFlash() throws Exception {
        Booking b = booking(null, Booking.BookingStatus.CONFIRMED, "899.00");
        b.setId(42L);
        when(bookingService.createBooking(anyLong(), anyString(), any(), any())).thenReturn(b);

        mvc.perform(post("/book")
                        .param("carId", "1").param("customerName", "Anna")
                        .param("startDate", "2026-07-01").param("endDate", "2026-07-04"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Bokning bekräftad! Boknings-ID: 42"));
    }

    @Test
    void uppbokadBilGerFelFlashIStalletForKrasch() throws Exception {
        when(bookingService.createBooking(anyLong(), anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("Car is not available for the selected dates"));

        mvc.perform(post("/book")
                        .param("carId", "1").param("customerName", "Anna")
                        .param("startDate", "2026-07-01").param("endDate", "2026-07-04"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void registreringOmdirigerarTillLoginMedKvitto() throws Exception {
        mvc.perform(post("/register").param("username", "ny").param("password", "hemligt"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("regSuccess"));
    }

    private static Booking booking(Car car, Booking.BookingStatus status, String price) {
        Booking b = new Booking();
        b.setCar(car);
        b.setStatus(status);
        b.setTotalPrice(new BigDecimal(price));
        return b;
    }
}
