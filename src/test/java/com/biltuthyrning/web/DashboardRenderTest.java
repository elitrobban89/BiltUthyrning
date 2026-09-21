package com.biltuthyrning.web;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.model.Car;
import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.CarService;
import com.biltuthyrning.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renderar dashboard.html på RIKTIGT — med Thymeleaf, inte bara vynamn och modell.
 *
 * <p><b>Varför provet finns:</b> 2026-09-21 svarade startsidan 500 i drift, och hade gjort
 * det sedan uppgraderingen till Java 25 + Boot 3.5.16. Felet satt i EN mall-rad:
 * {@code th:attr="... data-fuel=${@carUtils.getFuelType(car.engine)}"}. Thymeleaf kör
 * {@code th:attr} i ett BEGRÄNSAT läge där {@code @bean}-anrop är förbjudna — det gick i
 * Thymeleaf 3.1.2 och slutade gå i 3.1.5, som följde med Boot-uppgraderingen.
 *
 * <p><b>Varför ingen märkte det:</b> {@link WebControllerTest} bygger sin MockMvc med
 * {@code standaloneSetup} och en {@code InternalResourceViewResolver}. Den kontrollerar att
 * controllern returnerar vynamnet "dashboard" och rätt modell — men mallen renderas aldrig.
 * Hela sviten var grön medan sidan var nere. Ett prov som stannar vid vynamnet kan inte se
 * ett fel som bor i vyn.
 *
 * <p>Därför laddar det här provet en riktig webbkontext med Thymeleaf och läser HTML:en som
 * kommer ut. <b>Bilen i listan är inte pynt:</b> uttrycket ligger inne i {@code th:each}, så
 * en tom lista hade hoppat över exakt den rad som var trasig och gett grönt prov ändå.
 */
@WebMvcTest(controllers = WebController.class)
@AutoConfigureMockMvc(addFilters = false)   // ingen filterkedja: principalen sätts direkt nedan
@Import(CarUtils.class)                     // @carUtils måste vara en RIKTIG böna — det är den mallen frågar
class DashboardRenderTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private CarService carService;
    @MockitoBean private BookingService bookingService;
    @MockitoBean private UserService userService;

    @BeforeEach
    void loggaIn() {
        // @AuthenticationPrincipal läser ur SecurityContextHolder, som MockMvc delar tråd med.
        // Det räcker här och sparar ett beroende till spring-security-test.
        UserDetails admin = User.withUsername("admin").password("x").authorities("ROLE_ADMIN").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, "x",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        Car car = new Car();
        car.setId(1L);
        car.setModel("Volvo XC40");       // ett märke MED emblem, så bildvägen provas
        car.setYear("2023");
        car.setEngine("B4 AWD");
        car.setDailyRate(new BigDecimal("899.00"));

        Booking b = new Booking();
        b.setId(26L);
        b.setCar(car);
        b.setCustomerName("John Doe");
        b.setStartDate(LocalDate.of(2026, 9, 15));
        b.setEndDate(LocalDate.of(2026, 9, 19));
        b.setTotalPrice(new BigDecimal("3596.00"));
        b.setStatus(Booking.BookingStatus.CONFIRMED);

        when(carService.getAllCars()).thenReturn(List.of(car));
        when(bookingService.getAllBookings()).thenReturn(List.of(b));
    }

    @AfterEach
    void loggaUt() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void startsidanRenderasUtanFel() throws Exception {
        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("Volvo XC40")));
    }

    @Test
    void bilkortetBarSinaDataAttributUrBonan() throws Exception {
        // Det är de HÄR attributen uttrycket satte. Faller bönanropet tillbaka in i th:attr
        // blir svaret 500 i stället, och provet säger till före drift i stället för efter.
        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("data-car-id=\"1\"")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("data-fuel=")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("/emblem/volvo.svg")));
    }

    @Test
    void bootskarmenRitasBaraMedWelcomeParametern() throws Exception {
        // ?welcome=1 är vägen admin skickas till efter inloggning — den läser ${param.welcome},
        // och parametrar är en av sakerna Thymeleafs begränsade läge stänger av. Provet håller
        // den vägen öppen så att inloggningen inte landar på en 500 igen.
        mvc.perform(get("/?welcome=1"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("bu-boot")));

        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.not(
                   org.hamcrest.Matchers.containsString("id=\"bu-boot\""))));
    }
}
